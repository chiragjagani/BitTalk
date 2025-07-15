package com.cj.bittalk;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.cj.bittalk.adapters.DeviceAdapter;
import com.cj.bittalk.adapters.MessageAdapter;
import com.cj.bittalk.databinding.ActivityMainBinding;
import com.cj.bittalk.model.Device;
import com.cj.bittalk.model.Message;
import com.cj.bittalk.services.BluetoothService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements BluetoothService.BluetoothConnectionListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSIONS = 1;

    private ActivityMainBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothService bluetoothService;
    private MessageAdapter messageAdapter;
    private DeviceAdapter deviceAdapter;
    private AlertDialog deviceDialog;

    private LinearLayout emptyStateLayout;
    private MaterialButton disconnectButton;
    private MaterialButton getStartedButton;

    private final ActivityResultLauncher<Intent> enableBluetoothLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setupBluetooth();
                } else {
                    Toast.makeText(this, "Bluetooth is required for this app", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && deviceAdapter != null) {
                    if (hasBluetoothPermission()) {
                        deviceAdapter.addDevice(new Device(device));
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (deviceDialog != null && deviceDialog.isShowing()) {
                    View dialogView = deviceDialog.findViewById(R.id.scanningProgress);
                    if (dialogView != null) {
                        dialogView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Critical: Set window flags BEFORE setupViews
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        );

        setupViews();
        checkPermissions();
    }

    private void setupViews() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar);

        // Get references to new UI elements
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        disconnectButton = findViewById(R.id.disconnectButton);
        getStartedButton = findViewById(R.id.getStartedButton);

        // Setup RecyclerView for messages
        messageAdapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // This is important!
        binding.messagesRecyclerView.setLayoutManager(layoutManager);
        binding.messagesRecyclerView.setAdapter(messageAdapter);

        // Setup send button
        binding.sendButton.setOnClickListener(v -> sendMessage());
        binding.deviceSelectionFab.setOnClickListener(v -> showDeviceSelectionDialog());

        // Setup disconnect button
        if (disconnectButton != null) {
            disconnectButton.setOnClickListener(v -> showDisconnectDialog());
        }

        // Setup get started button in empty state
        if (getStartedButton != null) {
            getStartedButton.setOnClickListener(v -> showDeviceSelectionDialog());
        }

        // Setup input field with IME action
        binding.messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Auto-scroll when keyboard appears
        binding.messagesRecyclerView.addOnLayoutChangeListener((v, left, top, right, bottom,
                                                                oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && messageAdapter.getItemCount() > 0) {
                binding.messagesRecyclerView.postDelayed(() ->
                        binding.messagesRecyclerView.smoothScrollToPosition(
                                messageAdapter.getItemCount() - 1
                        ), 100);
            }
        });

        // Setup input field with IME action
        binding.messageInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });

        // Auto-scroll when keyboard appears
        binding.messagesRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom && messageAdapter.getItemCount() > 0) {
                    binding.messagesRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.messagesRecyclerView.smoothScrollToPosition(
                                    messageAdapter.getItemCount() - 1
                            );
                        }
                    }, 100);
                }
            }
        });

        // Setup device selection FAB
        binding.deviceSelectionFab.setOnClickListener(v -> showDeviceSelectionDialog());

        // Disable input until connected
        setInputEnabled(false);
        // Show empty state initially
        showEmptyState();
    }

    private void showEmptyState() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        }
        binding.messagesRecyclerView.setVisibility(View.GONE);
        binding.inputBarCard.setVisibility(View.GONE);
        binding.deviceSelectionFab.setExtended(true);
        setInputEnabled(false);
    }

    private void hideEmptyState() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
        binding.messagesRecyclerView.setVisibility(View.VISIBLE);
        binding.inputBarCard.setVisibility(View.VISIBLE);
        binding.deviceSelectionFab.setExtended(false);
    }

    private void showDisconnectDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Disconnect")
                .setMessage("Are you sure you want to disconnect? Your chat history will be cleared.")
                .setPositiveButton("Disconnect", (dialog, which) -> {
                    disconnect();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void disconnect() {
        if (bluetoothService != null) {
            bluetoothService.stop();
        }

        // Clear messages
        messageAdapter.clearMessages();

        // Reset UI
        showEmptyState();
        binding.toolbar.setSubtitle("Not connected");
        if (disconnectButton != null) {
            disconnectButton.setVisibility(View.GONE);
        }

        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    private boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        } else {
            setupBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                setupBluetooth();
            } else {
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void setupBluetooth() {
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBluetoothLauncher.launch(enableIntent);
            } else {
                bluetoothService = new BluetoothService(this, this);
                bluetoothService.start();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            checkPermissions();
        }
    }

    private void showDeviceSelectionDialog() {
        if (!hasBluetoothPermission()) {
            checkPermissions();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_device_selection, null);
        RecyclerView devicesRecyclerView = dialogView.findViewById(R.id.devicesRecyclerView);
        ProgressBar scanningProgress = dialogView.findViewById(R.id.scanningProgress);
        TextView noDevicesText = dialogView.findViewById(R.id.noDevicesText);

        deviceAdapter = new DeviceAdapter(device -> {
            connectToDevice(device);
            if (deviceDialog != null) {
                deviceDialog.dismiss();
            }
        });

        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        devicesRecyclerView.setAdapter(deviceAdapter);

        loadPairedDevices();

        deviceDialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Scan", (dialog, which) -> startDiscovery())
                .create();

        deviceDialog.show();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
    }

    private void loadPairedDevices() {
        if (!hasBluetoothPermission()) {
            return;
        }

        try {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            deviceAdapter.clearDevices();

            for (BluetoothDevice device : pairedDevices) {
                deviceAdapter.addDevice(new Device(device));
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Bluetooth permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void startDiscovery() {
        if (!hasBluetoothPermission()) {
            checkPermissions();
            return;
        }

        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            bluetoothAdapter.startDiscovery();

            if (deviceDialog != null && deviceDialog.isShowing()) {
                View scanningProgress = deviceDialog.findViewById(R.id.scanningProgress);
                if (scanningProgress != null) {
                    scanningProgress.setVisibility(View.VISIBLE);
                }
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Bluetooth permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void connectToDevice(Device device) {
        if (bluetoothService != null) {
            bluetoothService.connect(device.getBluetoothDevice());
        }
    }

    private void sendMessage() {
        String message = binding.messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(message) && bluetoothService != null
                && bluetoothService.getState() == BluetoothService.STATE_CONNECTED) {

            byte[] send = message.getBytes(StandardCharsets.UTF_8);
            bluetoothService.write(send);
            binding.messageInput.setText("");

            // Hide keyboard after sending
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void setInputEnabled(boolean enabled) {
        binding.messageInput.setEnabled(enabled);
        binding.sendButton.setEnabled(enabled);
        binding.messageInput.setHint(enabled ? "Type a message" : "Not connected");
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            binding.messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeight = 0;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    if (previousHeight != keypadHeight) {
                        binding.messagesRecyclerView.postDelayed(() -> scrollToBottom(), 100);
                    }
                }
                previousHeight = keypadHeight;
            }
        });
    }

    @Override
    public void onStateChanged(int state) {
        runOnUiThread(() -> {
            switch (state) {
                case BluetoothService.STATE_CONNECTED:
                    binding.toolbar.setSubtitle("Connected");
                    hideEmptyState();
                    setInputEnabled(true);
                    if (disconnectButton != null) {
                        disconnectButton.setVisibility(View.VISIBLE);
                    }
                    break;

                case BluetoothService.STATE_CONNECTING:
                    binding.toolbar.setSubtitle("Connecting...");
                    setInputEnabled(false);
                    break;

                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                    binding.toolbar.setSubtitle("Not connected");
                    showEmptyState();
                    setInputEnabled(false);
                    if (disconnectButton != null) {
                        disconnectButton.setVisibility(View.GONE);
                    }
                    break;
            }
        });
    }

    @Override
    public void onMessageReceived(String message) {
        runOnUiThread(() -> {
            messageAdapter.addMessage(new Message(message, false));
            // Auto scroll to new message
            binding.messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        });
    }

    @Override
    public void onMessageSent(String message) {
        runOnUiThread(() -> {
            messageAdapter.addMessage(new Message(message, true));
            // Auto scroll to new message
            binding.messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        });
    }

    @Override
    public void onConnectionFailed() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDeviceConnected(String deviceName) {
        runOnUiThread(() -> {
            binding.toolbar.setSubtitle("Connected to " + deviceName);
            Toast.makeText(this, "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
            // Show a welcome message in chat
            Message welcomeMsg = new Message(
                    "Connected to " + deviceName + ". You can now start chatting!",
                    false
            );
            welcomeMsg.setSystemMessage(true);
            messageAdapter.addMessage(welcomeMsg);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered
        }

        if (bluetoothService != null) {
            bluetoothService.stop();
        }

        if (bluetoothAdapter != null) {
            try {
                if (bluetoothAdapter.isDiscovering() && hasBluetoothPermission()) {
                    bluetoothAdapter.cancelDiscovery();
                }
            } catch (SecurityException e) {
                // Permission was revoked
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothService != null && bluetoothService.getState() == BluetoothService.STATE_NONE) {
            bluetoothService.start();
        }
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
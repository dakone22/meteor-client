/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2020 Meteor Development.
 */

package minegame159.meteorclient.modules.combat;

import baritone.api.BaritoneAPI;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.commands.CommandManager;
import minegame159.meteorclient.commands.commands.swarm.SwarmQueen;
import minegame159.meteorclient.commands.commands.swarm.SwarmSlave;
import minegame159.meteorclient.events.game.GameJoinedEvent;
import minegame159.meteorclient.events.game.GameLeftEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.gui.screens.WindowScreen;
import minegame159.meteorclient.gui.widgets.WButton;
import minegame159.meteorclient.gui.widgets.WLabel;
import minegame159.meteorclient.gui.widgets.WTable;
import minegame159.meteorclient.gui.widgets.WWidget;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.modules.player.InfinityMiner;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.settings.StringSetting;
import minegame159.meteorclient.utils.network.MeteorExecutor;
import minegame159.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;


/**
 * @author Inclemental
 * Special thanks to Eli for lending me the test account. Love you bud.
 */

public class Swarm extends Module {
    public Swarm() {
        super(Category.Combat, "Swarm", I18n.translate("Module.Swarm.description"));
    }

    public enum Mode {
        Queen,
        Slave,
        Idle
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> ipAddress = sgGeneral.add(new StringSetting.Builder()
            .name("ip-address")
            .displayName(I18n.translate("Module.Swarm.setting.ipAddress.displayName"))
            .description(I18n.translate("Module.Swarm.setting.ipAddress.description"))
            .defaultValue("localhost")
            .build());

    private final Setting<Integer> serverPort = sgGeneral.add(new IntSetting.Builder()
            .name("port")
            .displayName(I18n.translate("Module.Swarm.setting.serverPort.displayName"))
            .description(I18n.translate("Module.Swarm.setting.serverPort.description"))
            .defaultValue(7777)
            .sliderMin(1)
            .sliderMax(65535)
            .build());

    public SwarmServer server;
    public SwarmClient client;
    public BlockState targetBlock;
    public Mode currentMode = Mode.Idle;
    private WLabel label;

    @Override
    public void onActivate() {
        currentMode = Mode.Idle;
        closeAllServerConnections();
    }

    @Override
    public void onDeactivate() {
        currentMode = Mode.Idle;
        closeAllServerConnections();
    }

    @Override
    public WWidget getWidget() {
        WTable table = new WTable();
        label = new WLabel("");
        table.add(label);
        setLabel();
        table.row();
        WTable table2 = new WTable();
        WButton runServer = new WButton(I18n.translate("Module.Swarm.button.run"));
        runServer.action = this::runServer;
        table2.add(runServer);
        WButton connect = new WButton(I18n.translate("Module.Swarm.button.connect"));
        connect.action = this::runClient;
        table2.add(connect);
        WButton reset = new WButton(I18n.translate("Module.Swarm.button.reset"));
        reset.action = () -> {
            ChatUtils.moduleInfo(this, I18n.translate("Module.Swarm.message.closing_all"));
            closeAllServerConnections();
            currentMode = Mode.Idle;
            setLabel();
        };
        table2.add(reset);
        table.add(table2);
        table.row();
        WButton guide = new WButton(I18n.translate("Module.Swarm.button.guide"));
        guide.action = () -> MinecraftClient.getInstance().openScreen(new SwarmHelpScreen());
        table.add(guide);
        table.row();
        return table;
    }

    public void runServer() {
        if (server == null) {
            currentMode = Mode.Queen;
            setLabel();
            closeAllServerConnections();
            server = new SwarmServer();
        }
    }

    public void runClient() {
        if (client == null) {
            currentMode = Mode.Slave;
            setLabel();
            closeAllServerConnections();
            client = new SwarmClient();
        }
    }

    public void closeAllServerConnections() {
        try {
            if (server != null) {
                server.interrupt();
                server.close();
                server.serverSocket.close();
                server = null;
            }
            if (client != null) {
                client.interrupt();
                client.disconnect();
                client.socket.close();
                client = null;
            }
        } catch (Exception ignored) {
        }
    }

    private void setLabel() {
        if (currentMode != null)
            label.setText(I18n.translate("Module.Swarm.label.current_mode", I18n.translate("Module.Swarm.label.current_mode." + currentMode)));
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (targetBlock != null)
            mine();
    });

    public void idle() {
        currentMode = Mode.Idle;
        if (ModuleManager.INSTANCE.get(InfinityMiner.class).isActive())
            ModuleManager.INSTANCE.get(InfinityMiner.class).toggle();
        if (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    public void mine() {
        ChatUtils.moduleInfo(this, I18n.translate("Module.Swarm.message.mine_start"));
        if (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().mine(targetBlock.getBlock());
        targetBlock = null;

    }

    public class SwarmClient extends Thread {

        public Socket socket;
        public String ipAddress;

        SwarmClient() {
            ipAddress = Swarm.this.ipAddress.get();
            start();
        }

        @Override
        public void run() {
            InputStream inputStream;
            DataInputStream dataInputStream;
            try {
                while (socket == null && !isInterrupted()) {
                    try {
                        socket = new Socket(ipAddress, serverPort.get());
                    } catch (Exception ignored) {
                        ChatUtils.moduleWarning(ModuleManager.INSTANCE.get(Swarm.class), I18n.translate("Module.Swarm.message.not_found"));
                    }
                    if (socket == null) {
                        Thread.sleep(5000);
                    }
                }
                if (socket != null) {
                    inputStream = socket.getInputStream();
                    dataInputStream = new DataInputStream(inputStream);
                    ChatUtils.moduleInfo(ModuleManager.INSTANCE.get(Swarm.class), I18n.translate("Module.Swarm.message.new_socket"));
                    while (!isInterrupted()) {
                        if (socket != null) {
                            String read;
                            read = dataInputStream.readUTF();
                            if (!read.equals("")) {
                                ChatUtils.moduleInfo(ModuleManager.INSTANCE.get(Swarm.class), I18n.translate("Module.Swarm.message.new_command", read));
                                execute(read);
                            }
                        }
                    }
                    dataInputStream.close();
                    inputStream.close();
                }
            } catch (Exception e) {
                ChatUtils.moduleError(ModuleManager.INSTANCE.get(Swarm.class), I18n.translate("Module.Swarm.message.error_in_connection"));
                disconnect();
                client = null;
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        ChatUtils.moduleError(ModuleManager.INSTANCE.get(Swarm.class), I18n.translate("Module.Swarm.message.error_in_connection"));
                    }
                }
            }
        }

        public void disconnect() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public class SwarmServer extends Thread {
        private ServerSocket serverSocket;
        public final static int MAX_CLIENTS = 50;
        final private SubServer[] clientConnections = new SubServer[MAX_CLIENTS];

        public SwarmServer() {
            try {
                int port = serverPort.get();
                this.serverSocket = new ServerSocket(port);
                ChatUtils.moduleInfo(ModuleManager.INSTANCE.get(Swarm.class), I18n.translate("Module.Swarm.message.new_server", serverPort.get()));
                start();
            } catch (Exception ignored) {

            }
        }

        @Override
        public void run() {
            try {
                ChatUtils.moduleInfo(ModuleManager.INSTANCE.get(Swarm.class), I18n.translate("Module.Swarm.message.listening"));
                while (!this.isInterrupted()) {
                    Socket connection = this.serverSocket.accept();
                    assignConnectionToSubServer(connection);
                }
            } catch (Exception ignored) {
            }
        }

        public void assignConnectionToSubServer(Socket connection) {
            for (int i = 0; i < clientConnections.length; i++) {
                if (this.clientConnections[i] == null) {
                    this.clientConnections[i] = new SubServer(connection);
                    ChatUtils.moduleInfo(ModuleManager.INSTANCE.get(Swarm.class), I18n.translate("Module.Swarm.message.new_slave"));
                    break;
                }
            }
        }

        public void close() {
            try {
                interrupt();
                for (SubServer clientConnection : clientConnections) {
                    if (clientConnection != null) {
                        clientConnection.close();
                    }
                }
                serverSocket.close();
            } catch (Exception e) {
                ChatUtils.moduleInfo(ModuleManager.INSTANCE.get(Swarm.class), I18n.translate("Module.Swarm.message.server_closed"));
            }
        }

        public void closeAllClients() {
            try {
                for (SubServer s : clientConnections) {
                    if (s.connection != null)
                        s.close();
                }
            } catch (Exception e) {
                closeAllServerConnections();
            }
        }

        public synchronized void sendMessage(@Nonnull String s) {
            MeteorExecutor.execute(() -> {
                try {
                    for (SubServer clientConnection : clientConnections) {
                        if (clientConnection != null) {
                            clientConnection.messageToSend = s;
                        }
                    }
                } catch (Exception ignored) {
                }
            });

        }

    }

    public static class SubServer extends Thread {
        final private Socket connection;
        private volatile String messageToSend;

        public SubServer(@Nonnull Socket connection) {
            this.connection = connection;
            start();
        }

        @Override
        public void run() {
            OutputStream outputStream;
            DataOutputStream dataOutputStream;
            try {
                outputStream = connection.getOutputStream();
                dataOutputStream = new DataOutputStream(outputStream);
                while (!this.isInterrupted()) {
                    if (messageToSend != null) {
                        dataOutputStream.writeUTF(messageToSend);
                        dataOutputStream.flush();
                        messageToSend = null;
                    }
                }
                outputStream.close();
                dataOutputStream.close();
            } catch (Exception e) {
                ChatUtils.moduleError(ModuleManager.INSTANCE.get(Swarm.class), I18n.translate("Module.Swarm.message.error"));
            }
        }

        public void close() {
            try {
                interrupt();
                this.connection.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void execute(@Nonnull String s) {
        try {
            CommandManager.dispatch(s);
        } catch (CommandSyntaxException ignored) {
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<GameLeftEvent> gameLeftEventListener = new Listener<>(event -> {
        closeAllServerConnections();
        this.toggle();
    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<GameJoinedEvent> gameJoinedEventListener = new Listener<>(event -> {
        closeAllServerConnections();
        this.toggle();
    });

    private class SwarmHelpScreen extends WindowScreen {

        private final WTable textTable;
        private final WButton introButton;
        private final WButton ipConfigButton;
        private final WButton queenButton;
        private final WButton slaveButton;

        public SwarmHelpScreen() {
            super(I18n.translate("Module.Swarm.guide.title"), true);
            textTable = new WTable();
            introButton = new WButton(I18n.translate("Module.Swarm.guide.button.introduction"));
            introButton.action = () -> {
                buildTextTable(getSwarmGuideIntro());
                initWidgets();
            };
            ipConfigButton = new WButton(I18n.translate("Module.Swarm.guide.button.configuration"));
            ipConfigButton.action = () -> {
                buildTextTable(getSwarmGuideConfig());
                initWidgets();
            };
            queenButton = new WButton(I18n.translate("Module.Swarm.guide.button.queen"));
            queenButton.action = () -> {
                buildTextTable(getSwarmGuideQueen());
                initWidgets();
            };
            slaveButton = new WButton(I18n.translate("Module.Swarm.guide.button.slave"));
            slaveButton.action = () -> {
                buildTextTable(getSwarmGuideSlave());
                initWidgets();
            };
            buildTextTable(getSwarmGuideIntro());
            initWidgets();
        }

        private void initWidgets() {
            clear();
            WTable table = new WTable();
            table.add(introButton);
            table.add(ipConfigButton);
            table.add(queenButton);
            table.add(slaveButton);
            add(table);
            row();
            add(textTable);
            row();
        }

        private void buildTextTable(List<String> text) {
            textTable.clear();
            for (String s : text) {
                textTable.add(new WLabel(s));
                textTable.row();
            }
        }
    }

    // compressing strings so they could fit in some width
    private List<String> fitLines(List<String> lines, String sep) {
        int maxLineLength = 100;
        List<String> newList = new java.util.ArrayList<>();

        for (String line : lines) {
            while (line.length() > maxLineLength) {
                String subLine = line.substring(0, maxLineLength + 1);
                if (!subLine.contains(" ")) break;

                int i = subLine.lastIndexOf(" ");
                newList.add(line.substring(0, i));
                line = sep + line.substring(i + 1);
            }
            newList.add(line);
        }

        return newList;
    }

    private List<String> fitLines(List<String> lines) {
        return fitLines(lines, "");
    }

    //I know its ugly, I don't care.
    private List<String> getSwarmGuideIntro() {
        return fitLines(Arrays.asList(
                I18n.translate("Module.Swarm.guide.introduction.line1"), "",
                I18n.translate("Module.Swarm.guide.introduction.line2"), "",
                I18n.translate("Module.Swarm.guide.introduction.line3"), "",
                I18n.translate("Module.Swarm.guide.introduction.line4", CommandManager.get(SwarmQueen.class).toString())
        ));
    }

    private List<String> getSwarmGuideConfig() {
        return fitLines(Arrays.asList(
                I18n.translate("Module.Swarm.guide.configuration.line1"),
                I18n.translate("Module.Swarm.guide.configuration.line2"),
                "",
                I18n.translate("Module.Swarm.guide.configuration.line3"),
                I18n.translate("Module.Swarm.guide.configuration.line4"),
                "",
                I18n.translate("Module.Swarm.guide.configuration.line5"),
                I18n.translate("Module.Swarm.guide.configuration.line6")
        ), " ");
    }

    private List<String> getSwarmGuideQueen() {
        return fitLines(Arrays.asList(
                I18n.translate("Module.Swarm.guide.queen.line1"),
                I18n.translate("Module.Swarm.guide.queen.line2"),
                I18n.translate("Module.Swarm.guide.queen.line3"),
                I18n.translate("Module.Swarm.guide.queen.line4"),
                I18n.translate("Module.Swarm.guide.queen.line5", CommandManager.get(SwarmQueen.class).toString("queen"))
        ), " ");
    }

    private List<String> getSwarmGuideSlave() {
        return fitLines(Arrays.asList(
                I18n.translate("Module.Swarm.guide.slave.line1"),
                I18n.translate("Module.Swarm.guide.slave.line2", CommandManager.get(SwarmSlave.class).toString("slave"))
        ), " ");
    }

}
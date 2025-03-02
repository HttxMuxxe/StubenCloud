package eu.pixelstube.cloud.console;

import eu.pixelstube.cloud.CloudLauncher;
import eu.pixelstube.cloud.command.ICommandHandler;
import eu.pixelstube.cloud.console.setup.SetupBuilder;
import org.apache.commons.io.Charsets;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

/**
 * This file was created by Max H. (Haizoooon)
 * Date: 15.05.2021
 * Copyright© 2021 Max H.
 **/
public class ConsoleManager {

    private final LineReader lineReader;
    private Thread thread;
    private final ConsoleCompleter consoleCompleter;

    private final String prompt =
            Color.RESET.getColor()
            + Color.WHITE.getColor()
            + System.getProperty("user.name")
            + Color.RESET.getColor() + "@" + Color.CYAN.getColor()
            + "stubencloud-v1.0.0"
            + Color.RESET.getColor() + " $ ";

    public ConsoleManager() {
        consoleCompleter = new ConsoleCompleter();
        lineReader = createLineReader();
        startThread();
    }

    private LineReader createLineReader(){

        Terminal terminal = null;
        try {
            System.setProperty("org.jline.terminal.dumb", "true");
            terminal = TerminalBuilder.builder().system(true).streams(System.in, System.out).encoding(Charsets.UTF_8).dumb(true).build();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return LineReaderBuilder.builder()
                .completer(consoleCompleter)
                .terminal(terminal)
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .option(LineReader.Option.AUTO_REMOVE_SLASH, false)
                .option(LineReader.Option.INSERT_TAB, false)
                .build();
    }

    public void startThread(){
        thread = new Thread(() -> {
            try {
                String line;
                while(!Thread.currentThread().isInterrupted()){
                    line = lineReader.readLine(prompt);
                    handleInput(line);
                }
            }catch (UserInterruptException ignored){}
        });
        thread.start();
    }

    public void handleInput(String input){

        if(SetupBuilder.getCurrentSetup() != null){
            SetupBuilder.nextQuestion(SetupBuilder.getCurrentSetup().getCurrentInput().handle(input));
            return;
        }

        if(input.isEmpty()){
            return;
        }

        String[] args = input.split(" ");
        String command = args[0];
        if(CloudLauncher.getInstance().getCommandManager().getCommandHandlerByName(command) == null){
            CloudLauncher.getInstance().getCloudLogger().warning("The command could not be found! Please type 'help' for help.");
            return;
        }
        ICommandHandler commandHandler = CloudLauncher.getInstance().getCommandManager().getCommandHandlerByName(command);
        commandHandler.handle(CloudLauncher.getInstance().getConsoleSender(), args);
    }

    public void stopThread() {
        lineReader.getTerminal().reader().shutdown();
        lineReader.getTerminal().pause();
        thread.interrupt();
    }

    public LineReader getLineReader() {
        return lineReader;
    }

    public ConsoleCompleter getConsoleCompleter() {
        return consoleCompleter;
    }

    public String getPrefix() {
        return prompt;
    }

}

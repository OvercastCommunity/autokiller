package net.climaxmc.autokiller.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.entity.Player;

public class LogFile {

  private final AutoKiller plugin;

  private File file;

  public LogFile(AutoKiller plugin) {
    this.plugin = plugin;
  }

  public void write(Player player, String string) {

    File dataFolder = plugin.getDataFolder();
    if (!dataFolder.exists()) {
      dataFolder.mkdir();
    }

    File logsFolder = new File(plugin.getDataFolder() + File.separator + "logs");
    if (!logsFolder.exists()) {
      logsFolder.mkdirs();
    }

    this.file =
        new File(plugin.getDataFolder() + File.separator + "logs", player.getName() + ".txt");
    if (!file.exists()) {
      try {
        file.createNewFile();

      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(file, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    PrintWriter printWriter = new PrintWriter(fileWriter);
    printWriter.println(string);
    printWriter.flush();
    printWriter.close();
  }
}

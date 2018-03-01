package chat.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chat.base.ChatCommand;
import chat.base.CommandName;
import javafx.util.converter.CharacterStringConverter;

class ChatClientUnitTest {

  @BeforeEach
  void setUp() throws Exception {}

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  @DisplayName("ChatCommand constructor with message string test.")
  void chatCommandTest() {

    assertAll("empty string", () -> {
      ChatCommand actual = new ChatCommand("");
      assertEquals(CommandName.CMDERR, actual.getCommand());
      assertEquals("", actual.getMessage());
      assertEquals("", actual.getPayload());
    });

    assertAll("one space", () -> {
      ChatCommand actual = new ChatCommand(" ");
      assertEquals(CommandName.CMDMSG, actual.getCommand());
      assertEquals(" ", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("two space", () -> {
      ChatCommand actual = new ChatCommand("  ");
      assertEquals(CommandName.CMDMSG, actual.getCommand());
      assertEquals("  ", actual.getMessage());
      assertEquals("", actual.getPayload());
    });

    assertAll("enter command", () -> {
      ChatCommand actual = new ChatCommand("/enTer");
      assertEquals(CommandName.CMDENTER, actual.getCommand());
      assertEquals("", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("enter command", () -> {
      ChatCommand actual = new ChatCommand("/enTer ");
      assertEquals(CommandName.CMDENTER, actual.getCommand());
      assertEquals("", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("enter command", () -> {
      ChatCommand actual = new ChatCommand(" /eNter ");
      assertEquals(CommandName.CMDENTER, actual.getCommand());
      assertEquals("", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("enter command", () -> {
      ChatCommand actual = new ChatCommand(" /eNter  ");
      assertEquals(CommandName.CMDENTER, actual.getCommand());
      assertEquals("", actual.getMessage());
      assertEquals(" ", actual.getPayload());
    });
    assertAll("enter command", () -> {
      ChatCommand actual = new ChatCommand(" /enter  oLeg");
      assertEquals(CommandName.CMDENTER, actual.getCommand());
      assertEquals("", actual.getMessage());
      assertEquals(" oLeg", actual.getPayload());
    });

    assertAll("exit command", () -> {
      ChatCommand actual = new ChatCommand("/eXIT");
      assertEquals(CommandName.CMDEXIT, actual.getCommand());
      assertEquals("", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("exit command", () -> {
      ChatCommand actual = new ChatCommand("/eXit ");
      assertEquals(CommandName.CMDEXIT, actual.getCommand());
      assertEquals("", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("exit command", () -> {
      ChatCommand actual = new ChatCommand("/eXit ");
      assertEquals(CommandName.CMDEXIT, actual.getCommand());
      assertEquals("", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("exit command", () -> {
      ChatCommand actual = new ChatCommand(" /eXit  Test ");
      assertEquals(CommandName.CMDEXIT, actual.getCommand());
      assertEquals("", actual.getMessage());
      assertEquals(" Test ", actual.getPayload());
    });
    
    assertAll("msg command", () -> {
      ChatCommand actual = new ChatCommand(" /string ");
      assertEquals(CommandName.CMDMSG, actual.getCommand());
      assertEquals(" /string ", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("msg command", () -> {
      ChatCommand actual = new ChatCommand(" /msg  /stRing ");
      assertEquals(CommandName.CMDMSG, actual.getCommand());
      assertEquals(" /stRing ", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("msg command", () -> {
      ChatCommand actual = new ChatCommand(" /string1  string2 ");
      assertEquals(CommandName.CMDMSG, actual.getCommand());
      assertEquals(" /string1  string2 ", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("msg command", () -> {
      ChatCommand actual = new ChatCommand("  /msg   1/string1  string2  /enter  ");
      assertEquals(CommandName.CMDMSG, actual.getCommand());
      assertEquals("  1/string1  string2  /enter  ", actual.getMessage());
      assertEquals("", actual.getPayload());
    });

    assertAll("other command", () -> {
      ChatCommand actual = new ChatCommand("  /usrlst test1 test2 ");
      assertEquals(CommandName.CMDERR, actual.getCommand());
      assertEquals("  /usrlst test1 test2 ", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    assertAll("other command", () -> {
      ChatCommand actual = new ChatCommand("  /err test1 test2 ");
      assertEquals(CommandName.CMDERR, actual.getCommand());
      assertEquals("  /err test1 test2 ", actual.getMessage());
      assertEquals("", actual.getPayload());
    });
    
    
    /*
     * assertEquals(new ChatCommand(CommandName.CMDMSG, " /string "), new ChatCommand(" /string "));
     * assertEquals(new ChatCommand(CommandName.CMDMSG, "  /string "), new
     * ChatCommand(" /msg  /string ")); assertEquals(new ChatCommand(CommandName.CMDMSG,
     * " string1    string2 "), new ChatCommand(" string1    string2 ")); assertEquals(new
     * ChatCommand(CommandName.CMDMSG, " string1    string2 "), new
     * ChatCommand(" /msg string1    string2 "));
     */

  }

}

package chat.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import chat.base.ChatCommand;
import chat.base.CommandName;

class ChatClientUnitTest {

  static class ChatCommandCompare extends ChatCommand {
    private static final long serialVersionUID = 1L;

    public ChatCommandCompare(String string) {
      super(string);
    }

    public ChatCommandCompare(CommandName cmdmsg, String string) {
      super(cmdmsg, string);
    }

    public ChatCommandCompare(CommandName cmdenter, String string, String string2) {
      super(cmdenter, string, string2);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ChatCommand other = (ChatCommand) obj;
      if (this.getCommand() == other.getCommand() && this.getPayload().equals(other.getPayload())
          && this.getMessage().equals(other.getMessage()))
        return true;
      return false;
    }
  }

  @DisplayName("Parametrized test.")
  @ParameterizedTest
  @MethodSource("chatCommandProvider")
  void chatTestParametrized(ChatCommandCompare expected, ChatCommandCompare actual) {
    assertEquals(expected, actual);
  }

  static Stream<Arguments> chatCommandProvider() {
    return Stream.of(
        Arguments.of(new ChatCommandCompare(CommandName.CMDMSG, "test "),
            new ChatCommandCompare(" /msg test ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDENTER, ""),
            new ChatCommandCompare(" /enTer ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDMSG, " /stRing ", ""),
            new ChatCommandCompare(" /msg  /stRing ")));
  }

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

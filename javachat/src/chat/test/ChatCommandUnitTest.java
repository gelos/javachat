package chat.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import chat.base.ChatCommand;
import chat.base.CommandName;

@DisplayName("ChatCommand constructor tests ")
class ChatCommandUnitTest {

  static class ChatCommandExtendedCompare extends ChatCommand {
    private static final long serialVersionUID = 1L;

    public ChatCommandExtendedCompare(String commandString) {
      super(commandString);
    }

    public ChatCommandExtendedCompare(CommandName commandName, String message) {
      super(commandName, message);
    }

    public ChatCommandExtendedCompare(CommandName commandName, String message, String payload) {
      super(commandName, message, payload);
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
      if (this.getCommandName() == other.getCommandName()
          && this.getPayload().equals(other.getPayload())
          && this.getMessage().equals(other.getMessage()))
        return true;
      return false;
    }
  }

  @DisplayName("with error command.")
  @ParameterizedTest
  @MethodSource("ErrorCommandProvider")
  void testErrorCommand(ChatCommandExtendedCompare expectedCommnad,
      ChatCommandExtendedCompare actualCommand) {
    assertEquals(expectedCommnad, actualCommand);
  }

  static Stream<Arguments> ErrorCommandProvider() {
    return Stream.of(
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDERR, ""),
            new ChatCommandExtendedCompare("")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDERR, "  /usrlst test1 test2 "),
            new ChatCommandExtendedCompare("  /usrlst test1 test2 ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDERR, "  /prvMSG 'test1 test2 "),
            new ChatCommandExtendedCompare("  /prvMSG 'test1 test2 ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDERR, "  /prvMSG 'test1' "),
            new ChatCommandExtendedCompare("  /prvMSG 'test1' ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDERR, "  /err test1 test2 ", ""),
            new ChatCommandExtendedCompare("  /err test1 test2 ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDERR, "  /prvMsg '' "),
            new ChatCommandExtendedCompare("  /prvMsg '' ")),
        Arguments.of(
            new ChatCommandExtendedCompare(CommandName.CMDERR,
                "  /prvMsg ehvberuvberuyv '' user11  user1013 test'  mesaage 2 + message три   "),
            new ChatCommandExtendedCompare(
                "  /prvMsg ehvberuvberuyv '' user11  user1013 test'  mesaage 2 + message три   ")));
  }

  @DisplayName("with normal message.")
  @ParameterizedTest
  @MethodSource("NormalCommandProvider")
  void testNormalCommand(ChatCommandExtendedCompare expectedCommnad,
      ChatCommandExtendedCompare actualCommand) {
    assertEquals(expectedCommnad, actualCommand);
  }

  static Stream<Arguments> NormalCommandProvider() {
    return Stream.of(

        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDMSG, " "),
            new ChatCommandExtendedCompare(" ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDMSG, "  "),
            new ChatCommandExtendedCompare("  ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDMSG, " /string "),
            new ChatCommandExtendedCompare(" /string ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDMSG, " /stRing "),
            new ChatCommandExtendedCompare(" /msg  /stRing ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDMSG, " /string1  string2 "),
            new ChatCommandExtendedCompare(" /string1  string2 ")),
        Arguments.of(
            new ChatCommandExtendedCompare(CommandName.CMDMSG, "  1/string1  string2  /enter  "),
            new ChatCommandExtendedCompare("  /msg   1/string1  string2  /enter  ")));

  }

  @DisplayName("with enter command.")
  @ParameterizedTest
  @MethodSource("EnterCommandProvider")
  void testEnterCommand(ChatCommandExtendedCompare expectedCommnad,
      ChatCommandExtendedCompare actualCommand) {
    assertEquals(expectedCommnad, actualCommand);
  }

  static Stream<Arguments> EnterCommandProvider() {
    return Stream.of(
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDENTER, ""),
            new ChatCommandExtendedCompare("/enTer")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDENTER, ""),
            new ChatCommandExtendedCompare("/enTer ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDENTER, ""),
            new ChatCommandExtendedCompare(" /enTer ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDENTER, "", " "),
            new ChatCommandExtendedCompare(" /enTer  ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDENTER, "", " oLeg "),
            new ChatCommandExtendedCompare(" /enTer  oLeg ")));

  }

  @DisplayName("with exit command.")
  @ParameterizedTest
  @MethodSource("ExitCommandProvider")
  void testExitCommand(ChatCommandExtendedCompare expectedCommnad,
      ChatCommandExtendedCompare actualCommand) {
    assertEquals(expectedCommnad, actualCommand);
  }

  static Stream<Arguments> ExitCommandProvider() {
    return Stream.of(

        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDEXIT, ""),
            new ChatCommandExtendedCompare("/eXIT")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDEXIT, ""),
            new ChatCommandExtendedCompare("/eXIT ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDEXIT, ""),
            new ChatCommandExtendedCompare(" /eXIT ")),
        Arguments.of(new ChatCommandExtendedCompare(CommandName.CMDEXIT, "", "  Test "),
            new ChatCommandExtendedCompare(" /eXIT   Test ")));
  }

  @DisplayName("with private message command.")
  @ParameterizedTest
  @MethodSource("PrivateMessageCommandProvider")
  void testPrivateMessageCommand(ChatCommandExtendedCompare expectedCommnad,
      ChatCommandExtendedCompare actualCommand) {
    assertEquals(expectedCommnad, actualCommand);
  }

  static Stream<Arguments> PrivateMessageCommandProvider() {
    return Stream.of(

        Arguments.of(
            new ChatCommandExtendedCompare(CommandName.CMDPRVMSG, " mesaage 2 + message три   ",
                "user11,name with spaces user1013"),
            new ChatCommandExtendedCompare(
                "  /prvMsg ' user11, name with spaces user1013 '  mesaage 2 + message три   ")),
        Arguments.of(
            new ChatCommandExtendedCompare(CommandName.CMDPRVMSG, " mesaage 2 + message три   ",
                "user11,user1013,test"),
            new ChatCommandExtendedCompare(
                "  /prvMsg ' user11  ,user1013 ,,test'  mesaage 2 + message три   ")));
  }
}

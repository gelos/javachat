package chat.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
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
      if (this.getCommandName() == other.getCommandName() && this.getPayload().equals(other.getPayload())
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
        Arguments.of(new ChatCommandCompare(CommandName.CMDERR, ""), new ChatCommandCompare("")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDERR, "  /usrlst test1 test2 "),
            new ChatCommandCompare("  /usrlst test1 test2 ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDERR, "  /err test1 test2 ", ""),
            new ChatCommandCompare("  /err test1 test2 ")),

        Arguments.of(new ChatCommandCompare(CommandName.CMDMSG, " "), new ChatCommandCompare(" ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDMSG, "  "),
            new ChatCommandCompare("  ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDMSG, " /string "),
            new ChatCommandCompare(" /string ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDMSG, " /stRing "),
            new ChatCommandCompare(" /msg  /stRing ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDMSG, " /string1  string2 "),
            new ChatCommandCompare(" /string1  string2 ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDMSG, "  1/string1  string2  /enter  "),
            new ChatCommandCompare("  /msg   1/string1  string2  /enter  ")),

        Arguments.of(new ChatCommandCompare(CommandName.CMDENTER, ""),
            new ChatCommandCompare("/enTer")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDENTER, ""),
            new ChatCommandCompare("/enTer ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDENTER, ""),
            new ChatCommandCompare(" /enTer ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDENTER, "", " "),
            new ChatCommandCompare(" /enTer  ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDENTER, "", " oLeg "),
            new ChatCommandCompare(" /enTer  oLeg ")),

        Arguments.of(new ChatCommandCompare(CommandName.CMDEXIT, ""),
            new ChatCommandCompare("/eXIT")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDEXIT, ""),
            new ChatCommandCompare("/eXIT ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDEXIT, ""),
            new ChatCommandCompare(" /eXIT ")),
        Arguments.of(new ChatCommandCompare(CommandName.CMDEXIT, "", "  Test "),
            new ChatCommandCompare(" /eXIT   Test ")));
  }

}

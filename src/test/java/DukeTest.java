import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import exception.DukeException;
import exception.DukeInvalidArgumentsException;
import exception.DukeInvalidInputException;

public class DukeTest {

    @Test
    public void parseInputDeleteRemovesTaskFromList() throws DukeException {
        // Setup
        Duke.tasks = new ArrayList<>();

        // Test
        Duke.parseInput("todo task 1");
        Duke.parseInput("todo task 2");
        Duke.parseInput("todo task 3");
        Duke.parseInput("delete 2");

        assertEquals(2, Duke.tasks.size());
        assertEquals(Duke.tasks.get(0).getTaskInfo(), "task 1");
        assertEquals(Duke.tasks.get(1).getTaskInfo(), "task 3");
    }

    @Test
    public void chatLoopExceptionThrownPrintsMessageToOutput() throws IOException {
        // Setup
        Duke.tasks = new ArrayList<>(100);

        // Test
        String testInput = "invalid input\nevent test /at too many /at date\n";

        InputStream in = makeInputStreamFromString(testInput);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Duke.chatLoop(in, out);

        final String expectedOutput = "Invalid Input: invalid input\n"
                + "Command event encountered invalid arguments: There are too many date arguments\n";
        assertEquals(expectedOutput, new String(out.toByteArray()));
    }

    @Test
    public void parseInputInvalidInputThrowException() {
        DukeInvalidInputException exception = assertThrows(DukeInvalidInputException.class, () -> {
            Duke.parseInput("invalid input");
        });

        assertEquals("invalid input", exception.getInvalidInput());
    }

    @Test
    public void parseInputTodoNoArgumentsThrowException() {
        DukeInvalidArgumentsException exception = assertThrows(DukeInvalidArgumentsException.class, () -> {
            Duke.parseInput("todo");
        });

        assertEquals("todo", exception.getCommand());
        assertEquals("The description of a todo cannot be empty", exception.getError());
    }

    @Test
    public void parseInputDeadlineNoArgumentsThrowException() {
        DukeInvalidArgumentsException exception = assertThrows(DukeInvalidArgumentsException.class, () -> {
            Duke.parseInput("deadline");
        });

        assertEquals("deadline", exception.getCommand());
        assertEquals("The description of a deadline cannot be empty", exception.getError());
    }

    @Test
    public void parseInputDeadlineInsufficientArgumentsThrowException() {
        DukeInvalidArgumentsException exception = assertThrows(DukeInvalidArgumentsException.class, () -> {
            Duke.parseInput("deadline test event no date");
        });

        assertEquals("deadline", exception.getCommand());
        assertEquals("The date for a deadline cannot be empty", exception.getError());
    }

    @Test
    public void parseInputDeadlineTooManyArgumentsThrowException() {
        DukeInvalidArgumentsException exception = assertThrows(DukeInvalidArgumentsException.class, () -> {
            Duke.parseInput("deadline test event /by too many /by dates");
        });

        assertEquals("deadline", exception.getCommand());
        assertEquals("There are too many date arguments", exception.getError());
    }

    @Test
    public void parseInputEventNoArgumentsThrowException() {
        DukeInvalidArgumentsException exception = assertThrows(DukeInvalidArgumentsException.class, () -> {
            Duke.parseInput("event");
        });

        assertEquals("event", exception.getCommand());
        assertEquals("The description of an event cannot be empty", exception.getError());
    }

    @Test
    public void parseInputEventInsufficientArgumentsThrowException() {
        DukeInvalidArgumentsException exception = assertThrows(DukeInvalidArgumentsException.class, () -> {
            Duke.parseInput("event test event no date");
        });

        assertEquals("event", exception.getCommand());
        assertEquals("The date for an event cannot be empty", exception.getError());
    }

    @Test
    public void parseInputEventTooManyArgumentsThrowException() {
        DukeInvalidArgumentsException exception = assertThrows(DukeInvalidArgumentsException.class, () -> {
            Duke.parseInput("event test event /at too many /at dates");
        });

        assertEquals("event", exception.getCommand());
        assertEquals("There are too many date arguments", exception.getError());
    }

    @Test
    public void parseInputNewDeadlineTask() throws DukeException {
        // Setup
        Duke.tasks = new ArrayList<>(100);

        // Test
        final String expectedOutput = "Got it. I've added this task:\n  [D][✗] return book (by: Sunday)\nNow you have 1 tasks in the list.";
        assertEquals(expectedOutput, Duke.parseInput("deadline return book /by Sunday"));

        assertEquals("return book", Duke.tasks.get(0).getTaskInfo());
        assertEquals(false, Duke.tasks.get(0).getCompletionState());
        assertEquals("[D][✗] return book (by: Sunday)", Duke.tasks.get(0).toString());
        assertTrue(Duke.tasks.get(0) instanceof DeadlineTask);
    }

    @Test
    public void parseInputNewEventTask() throws DukeException {
        // Setup
        Duke.tasks = new ArrayList<>(100);

        // Test
        final String expectedOutput = "Got it. I've added this task:\n  [E][✗] project meeting (at: Mon 2-4pm)\nNow you have 1 tasks in the list.";
        assertEquals(expectedOutput, Duke.parseInput("event project meeting /at Mon 2-4pm"));

        assertEquals("project meeting", Duke.tasks.get(0).getTaskInfo());
        assertEquals(false, Duke.tasks.get(0).getCompletionState());
        assertEquals("[E][✗] project meeting (at: Mon 2-4pm)", Duke.tasks.get(0).toString());
        assertTrue(Duke.tasks.get(0) instanceof EventTask);
    }

    @Test
    public void parseInputNewTodoTask() throws DukeException {
        // Setup
        Duke.tasks = new ArrayList<>(100);

        // Test
        final String expectedOutput = "Got it. I've added this task:\n  [T][✗] borrow book\nNow you have 1 tasks in the list.";
        assertEquals(expectedOutput, Duke.parseInput("todo borrow book"));

        assertEquals("borrow book", Duke.tasks.get(0).getTaskInfo());
        assertEquals(false, Duke.tasks.get(0).getCompletionState());
        assertEquals("[T][✗] borrow book", Duke.tasks.get(0).toString());
        assertTrue(Duke.tasks.get(0) instanceof TodoTask);
    }

    @Test
    public void chatLoopParseEventTaskInteractionOutput() throws IOException {
        // Setup
        Duke.tasks = new ArrayList<>(100);

        // Test
        String testInput = "event project meeting /at Mon 2-4pm\nlist\ndone 1\nlist\n";

        InputStream in = makeInputStreamFromString(testInput);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Duke.chatLoop(in, out);

        final String expectedOutput = "Got it. I've added this task:\n  [E][✗] project meeting (at: Mon 2-4pm)\nNow you have 1 tasks in the list.\n"
                + "1.[E][✗] project meeting (at: Mon 2-4pm)\n"
                + "Nice! I've marked this task as done:\n  [E][✓] project meeting (at: Mon 2-4pm)\n"
                + "1.[E][✓] project meeting (at: Mon 2-4pm)\n";
        assertEquals(expectedOutput, new String(out.toByteArray()));
    }

    @Test
    public void chatLoopParseDeadlineTaskInteractionOutput() throws IOException {
        // Setup
        Duke.tasks = new ArrayList<>(100);

        // Test
        String testInput = "deadline return book /by Sunday\nlist\ndone 1\nlist\n";

        InputStream in = makeInputStreamFromString(testInput);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Duke.chatLoop(in, out);

        final String expectedOutput = "Got it. I've added this task:\n  [D][✗] return book (by: Sunday)\nNow you have 1 tasks in the list.\n"
                + "1.[D][✗] return book (by: Sunday)\n"
                + "Nice! I've marked this task as done:\n  [D][✓] return book (by: Sunday)\n"
                + "1.[D][✓] return book (by: Sunday)\n";
        assertEquals(expectedOutput, new String(out.toByteArray()));
    }

    @Test
    public void chatLoopParseTodoTaskInteractionOutput() throws IOException {
        // Setup
        Duke.tasks = new ArrayList<>(100);

        // Test
        String testInput = "todo borrow book\nlist\ndone 1\nlist\n";

        InputStream in = makeInputStreamFromString(testInput);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Duke.chatLoop(in, out);

        final String expectedOutput = "Got it. I've added this task:\n  [T][✗] borrow book\nNow you have 1 tasks in the list.\n"
                + "1.[T][✗] borrow book\n" + "Nice! I've marked this task as done:\n  [T][✓] borrow book\n"
                + "1.[T][✓] borrow book\n";
        assertEquals(expectedOutput, new String(out.toByteArray()));
    }

    @Test
    public void chatLoopParseDoneSetsTaskAsDone() throws IOException {
        // Setup
        Duke.tasks = new ArrayList<>(100);

        // Test
        String testInput = "todo help people\n" + "todo help myself\n" + "done 1\n" + "list\n";

        InputStream in = makeInputStreamFromString(testInput);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Duke.chatLoop(in, out);

        final String expectedOutput = "Got it. I've added this task:\n  [T][✗] help people\nNow you have 1 tasks in the list.\n"
                + "Got it. I've added this task:\n  [T][✗] help myself\nNow you have 2 tasks in the list.\n"
                + "Nice! I've marked this task as done:\n  [T][✓] help people\n" + "1.[T][✓] help people\n"
                + "2.[T][✗] help myself\n";
        assertEquals(expectedOutput, new String(out.toByteArray()));
    }

    @Test
    public void parseInputDoneSetsTaskAsDone() throws DukeException {
        // Setup
        Duke.tasks = new ArrayList<>(100);
        Duke.parseInput("todo help people");

        // Test
        assertEquals("help people", Duke.tasks.get(0).getTaskInfo());
        assertEquals(false, Duke.tasks.get(0).getCompletionState());
        assertEquals("[T][✗] help people", Duke.tasks.get(0).toString());

        final String expectedOutput = "Nice! I've marked this task as done:\n  [T][✓] help people";
        assertEquals(expectedOutput, Duke.parseInput("done 1"));

        assertEquals("help people", Duke.tasks.get(0).getTaskInfo());
        assertEquals(true, Duke.tasks.get(0).getCompletionState());
        assertEquals("[T][✓] help people", Duke.tasks.get(0).toString());
    }

    @Test
    public void parseInputAddToList() throws DukeException {
        int tasksStateLength = Duke.tasks.size();

        Duke.parseInput("todo help people");

        assertEquals(tasksStateLength + 1, Duke.tasks.size());

        assertEquals("help people", Duke.tasks.get(tasksStateLength).getTaskInfo());
        assertEquals(false, Duke.tasks.get(tasksStateLength).getCompletionState());
        assertEquals("[T][✗] help people", Duke.tasks.get(tasksStateLength).toString());
    }

    @Test
    public void parseInputAddToListMaintainsOrder() throws DukeException {
        int tasksStateLength = Duke.tasks.size();

        Duke.parseInput("todo help people");
        Duke.parseInput("todo blah");
        Duke.parseInput("todo read book");

        assertEquals(tasksStateLength + 3, Duke.tasks.size());

        assertEquals("help people", Duke.tasks.get(tasksStateLength).getTaskInfo());
        assertEquals("blah", Duke.tasks.get(tasksStateLength + 1).getTaskInfo());
        assertEquals("read book", Duke.tasks.get(tasksStateLength + 2).getTaskInfo());
    }

    @Test
    public void parseInputListOutputTasksState() throws DukeException {
        // Setup
        Duke.tasks = new ArrayList<>(100);
        Duke.parseInput("todo help people");
        Duke.parseInput("todo blah");
        Duke.parseInput("todo read book");

        final String expectedString = "1.[T][✗] help people\n2.[T][✗] blah\n3.[T][✗] read book";

        // Test
        assertEquals(expectedString, Duke.parseInput("list"));
    }

    @Test
    public void parseInputByeOutputFarewell() throws DukeException {
        assertEquals("Bye. Hope to see you again soon!", Duke.parseInput("bye"));
    }

    @Test
    public void chatInputByeExitsLoop() throws IOException {
        String testInput = "bye\n" + "hello\n" + "bye\n";

        InputStream in = makeInputStreamFromString(testInput);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Duke.chatLoop(in, out);

        assertEquals("Bye. Hope to see you again soon!\n", new String(out.toByteArray()));
    }

    @Test
    public void chatLoopWritesToOutput() throws IOException {
        String testInput = "echoThis\n" + "echoThisAsWell\n" + "echoThisFinally\n";

        InputStream in = makeInputStreamFromString(testInput);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Duke.chatLoop(in, out);

        assertNotEquals("", new String(out.toByteArray()));
    }

    private ByteArrayInputStream makeInputStreamFromString(String testInput) {
        return new ByteArrayInputStream(testInput.getBytes(StandardCharsets.UTF_8));
    }
}
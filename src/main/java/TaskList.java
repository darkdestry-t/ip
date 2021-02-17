import java.io.IOException;
import java.util.HashMap;

import exception.DukeInvalidArgumentsException;

public class TaskList {

    private Storage storage;

    public TaskList(Storage storage) {
        this.storage = storage;
    }

    protected String executeEvent(HashMap<String, String> input) throws DukeInvalidArgumentsException {
        if (!input.containsKey("info")) {
            throw new DukeInvalidArgumentsException("event", "The description of an event cannot be empty");
        }
        if (!input.containsKey("at")) {
            throw new DukeInvalidArgumentsException("event", "The date for an event cannot be empty");
        }
        return addTaskAndReturnMessage(new EventTask(input.get("info"), input.get("at")));
    }

    protected String executeDeadline(HashMap<String, String> input) throws DukeInvalidArgumentsException {
        if (!input.containsKey("info")) {
            throw new DukeInvalidArgumentsException("deadline", "The description of a deadline cannot be empty");
        }
        if (!input.containsKey("by")) {
            throw new DukeInvalidArgumentsException("deadline", "The date for a deadline cannot be empty");
        }
        return addTaskAndReturnMessage(new DeadlineTask(input.get("info"), input.get("by")));
    }

    protected String executeTodo(HashMap<String, String> input) throws DukeInvalidArgumentsException {
        if (!input.containsKey("info")) {
            throw new DukeInvalidArgumentsException("todo", "The description of a todo cannot be empty");
        }
        return addTaskAndReturnMessage(new TodoTask(input.get("info")));
    }

    protected String executeList() {
        String output = "";
        for (int i = 0; i < storage.tasks.size(); i++) {
            output += String.format("%d.%s\n", i + 1, storage.tasks.get(i));
        }
        return output.substring(0, output.length() - 1);
    }

    protected String executeDelete(HashMap<String, String> tokenizedInput) {
        // TODO: Add Exception for out of range deletion
        Task t = storage.tasks.remove(Integer.parseInt(tokenizedInput.get("info")) - 1);
        return String.format("Noted. I've removed this task:\n  %s\nNow you have %d tasks in the list.",
                t.toString(), storage.tasks.size());
    }

    protected String executeDone(HashMap<String, String> tokenizedInput) {
        // TODO: Add Exception for out of range done
        Task t = storage.tasks.get(Integer.parseInt(tokenizedInput.get("info")) - 1);
        t.setTaskAsDone();
        return "Nice! I've marked this task as done:\n  " + t.toString();
    }

    protected String executeFind(HashMap<String, String> tokenizedInput) {
        String searchString = tokenizedInput.get("info");
        String output = "Here are the matching tasks in your list:\n";
        for (int i = 0; i < storage.tasks.size(); i++) {
            if (storage.tasks.get(i).getTaskInfo().contains(searchString)) {
                output += String.format("%d.%s\n", i + 1, storage.tasks.get(i));
            }
        }
        return output.substring(0, output.length() - 1);
    }

    protected String addTaskAndReturnMessage(Task task) {
        storage.tasks.add(task);
        return String.format("Got it. I've added this task:\n  %s\nNow you have %d tasks in the list.", task.toString(),
                storage.tasks.size());
    }

    public String executeArchive(HashMap<String, String> tokenizedInput) {
        Task t = storage.tasks.remove(Integer.parseInt(tokenizedInput.get("info")) - 1);
        t.setArchived(true);
        try {
            storage.archiveTask(t);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return String.format("Got it. I've added this task:\n  [E][X] project meeting (at: Mar 01 2020 1400)\nNow you have 1 tasks in the list.", t.toString(),
                storage.tasks.size());
    }
}

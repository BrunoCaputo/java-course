package br.com.brunocaputo.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.brunocaputo.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    taskModel.setUserId((UUID) request.getAttribute("userId"));

    LocalDateTime currentDate = LocalDateTime.now();
    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start or End date is before the current date!");
    }

    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date is after the end date!");
    }

    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.CREATED).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> listTasks(HttpServletRequest request) {
    List<TaskModel> tasks = this.taskRepository.findByUserId((UUID) request.getAttribute("userId"));

    return tasks;
  }

  @PutMapping("/{taskId}")
  public ResponseEntity updateTask(@RequestBody TaskModel taskModel, HttpServletRequest request,
      @PathVariable UUID taskId) {
    TaskModel task = this.taskRepository.findById(taskId).orElse(null);

    if (task == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task doesn't exist!");
    }

    if (!task.getUserId().equals((UUID) request.getAttribute("userId"))) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This task is not from this user!");
    }

    Utils.copyNonNullProperties(taskModel, task);

    TaskModel savedTask = this.taskRepository.save(task);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
  }
}

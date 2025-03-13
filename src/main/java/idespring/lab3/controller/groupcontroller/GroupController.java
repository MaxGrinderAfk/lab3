package idespring.lab3.controller.groupcontroller;

import idespring.lab3.model.Group;
import idespring.lab3.service.groupservice.GroupService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        List<Integer> studentIds = (List<Integer>) request.get("studentIds");

        Group group = groupService.addGroup(name, studentIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @GetMapping
    public ResponseEntity<List<Group>> getGroups(
            @RequestParam(required = false) String namePattern,
            @RequestParam(required = false) String sort) {
        List<Group> groups = groupService.readGroups(namePattern, sort);
        return !groups.isEmpty()
                ? new ResponseEntity<>(groups, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroupById(@Positive @NotNull @PathVariable Long groupId) {
        try {
            Group group = groupService.findById(groupId);
            return new ResponseEntity<>(group, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Group> getGroupByName(@NotEmpty @PathVariable String name) {
        try {
            Group group = groupService.findByName(name);
            return new ResponseEntity<>(group, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@Positive @NotNull @PathVariable Long groupId) {
        try {
            groupService.deleteGroup(groupId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/name/{name}")
    public ResponseEntity<Void> deleteGroupByName(@NotEmpty @PathVariable String name) {
        try {
            groupService.deleteGroupByName(name);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
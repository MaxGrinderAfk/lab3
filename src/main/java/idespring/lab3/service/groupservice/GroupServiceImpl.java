package idespring.lab3.service.groupservice;

import idespring.lab3.config.CacheConfig;
import idespring.lab3.model.Group;
import idespring.lab3.model.Student;
import idespring.lab3.repository.grouprepo.GroupRepository;
import idespring.lab3.repository.studentrepo.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final CacheConfig<String, Object> cache;
    private static final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository, StudentRepository studentRepository,
                            CacheConfig<String, Object> cache) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.cache = cache;
    }

    @Override
    public List<Group> readGroups(String namePattern, String sort) {
        String cacheKey = "allGroups" + namePattern + sort;
        List<Group> cachedGroups = (List<Group>) cache.get(cacheKey);
        if (cachedGroups != null) {
            return cachedGroups;
        }

        final long start = System.nanoTime();
        logger.info("Fetching groups with namePattern: {}, sort: {}", namePattern, sort);

        List<Group> groups;
        if (namePattern != null) {
            groups = groupRepository.findByNameContaining(namePattern);
        } else if (sort != null && sort.equalsIgnoreCase("asc")) {
            groups = groupRepository.findAllByOrderByNameAsc();
        } else {
            groups = groupRepository.findAll();
        }

        cache.put(cacheKey, groups);
        long end = System.nanoTime();
        logger.info("Execution time for readGroups: {} ms", (end - start) / 1_000_000);
        return groups;
    }

    @Override
    public Group findById(Long id) {
        String cacheKey = "group_" + id;
        Group cachedGroup = (Group) cache.get(cacheKey);
        if (cachedGroup != null) {
            return cachedGroup;
        }

        long start = System.nanoTime();
        logger.info("Fetching group by ID: {}", id);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + id));

        cache.put(cacheKey, group);
        long end = System.nanoTime();
        logger.info("Execution time for findById: {} ms", (end - start) / 1_000_000);
        return group;
    }

    @Override
    public Group findByName(String name) {
        String cacheKey = "name_" + name;
        Group cachedGroup = (Group) cache.get(cacheKey);
        if (cachedGroup != null) {
            return cachedGroup;
        }

        long start = System.nanoTime();
        logger.info("Fetching group by name: {}", name);

        Group group = groupRepository.findByName(name)
                .orElseThrow(() ->
                        new EntityNotFoundException("Group not found with name: " + name));

        cache.put(cacheKey, group);
        long end = System.nanoTime();
        logger.info("Execution time for findByName: {} ms", (end - start) / 1_000_000);
        return group;
    }

    @Override
    @Transactional
    public Group addGroup(String name, List<Integer> studentIds) {
        final long start = System.nanoTime();
        logger.info("Adding new group: {}", name);

        Group group = new Group(name);
        if (studentIds != null && !studentIds.isEmpty()) {
            List<Long> longStudentIds = studentIds.stream().map(Long::valueOf).toList();
            List<Student> students = studentRepository.findAllById(longStudentIds);

            if (students.size() != studentIds.size()) {
                Set<Long> foundStudentIds =
                        students.stream().map(Student::getId).collect(Collectors.toSet());
                List<Long> nonExistentIds = longStudentIds.stream()
                        .filter(id -> !foundStudentIds.contains(id)).toList();
                throw new
                        EntityNotFoundException("Студенты с ID " + nonExistentIds + " не найдены");
            }

            for (Student student : students) {
                student.setGroup(group);
            }
            group.setStudents(students);
        }

        Group savedGroup = groupRepository.save(group);
        cache.put("group_" + savedGroup.getId(), savedGroup);
        cache.put("name_" + savedGroup.getName(), savedGroup);
        cache.put("allGroups", null);

        long end = System.nanoTime();
        logger.info("Execution time for addGroup: {} ms", (end - start) / 1_000_000);
        return savedGroup;
    }

    private Group findByIdInternal(Long id) {
        return groupRepository.findById(id).orElse(null);
    }

    private Group findByNameInternal(String name) {
        return groupRepository.findByName(name).orElse(null);
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        logger.info("Deleting group with ID: {}", id);
        Group group = findByIdInternal(id);
        if (group == null) {
            throw new EntityNotFoundException("Group with ID " + id + " not found");
        }

        cache.remove("group_" + id);
        cache.remove("name_" + group.getName());
        cache.remove("allGroups");
        groupRepository.deleteById(id);
    }

    @Transactional
    public void deleteGroupByName(String name) {
        logger.info("Deleting group with name: {}", name);
        Group group = findByNameInternal(name);
        if (group == null) {
            throw new EntityNotFoundException("Group with name " + name + " not found");
        }

        cache.remove("name_" + name);
        cache.remove("group_" + group.getId());
        cache.remove("allGroups");
        groupRepository.deleteByName(name);
    }
}


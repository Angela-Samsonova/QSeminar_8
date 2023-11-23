package sberbank.service;

import org.junit.jupiter.api.*;
import ru.sberbank.data.Developer;
import ru.sberbank.data.Task;
import ru.sberbank.data.Tester;
import ru.sberbank.service.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessServiceTest {
    private final int EXPECTED_TASK_ID = 0;
    private final String EXPECTED_TASK_SUMMARY = "Тестовая задача";

    ProcessService processService;
    TaskService taskService;
    ReleaseService releaseService;
    TesterService testerService;
    DeveloperService developerService;

    @BeforeEach
    void setup() {
        Task developedAndTestedTask = new Task(1, "summary1");
        developedAndTestedTask.setTested(true);
        developedAndTestedTask.setDeveloped(true);
        Task task = new Task(2, "summary2");
        taskService = mock(TaskService.class);
        when(taskService.getTask(1)).thenReturn(developedAndTestedTask);
        when(taskService.getTask(2)).thenReturn(task);

        releaseService = mock(ReleaseService.class);
        testerService = mock(TesterService.class);
        developerService = mock(DeveloperService.class);
        processService = new ProcessService(taskService, developerService, testerService, releaseService);
    }

    @Test
    void pushStatusTask_failed_exception_with_developed_and_tested_task() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            processService.pushStatusTask(1);
        });

        assertEquals("Задача уже в финальном статусе!", exception.getMessage());
    }

    @Test
    void pushStatusTask_failed_exception_when_developed_is_empty() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            processService.pushStatusTask(2);
        });

        assertEquals("Нет свободных разработчиков!", exception.getMessage());
    }

    @Test
    void pushStatusTask_success() {
        Developer freeDev = new Developer(1, "Konstantin", "Nesterov");
        when(developerService.create(anyInt(), anyString(), anyString())).thenReturn(freeDev);
        ArrayList<Developer> devs = new ArrayList<>();
        devs.add(freeDev);
        when(developerService.getListOfFree()).thenReturn(devs);
        Tester freeTester = new Tester(1, "Andrey", "Kozin");
        when(testerService.create(anyInt(), anyString(), anyString())).thenReturn(freeTester);
        ArrayList<Tester> testers = new ArrayList<>();
        testers.add(freeTester);
        when(testerService.getListOfFree()).thenReturn(testers);

        processService.pushStatusTask(2);

        assertTrue(taskService.getTask(2).isDeveloped());
        assertTrue(taskService.getTask(2).isTested());
    }
}
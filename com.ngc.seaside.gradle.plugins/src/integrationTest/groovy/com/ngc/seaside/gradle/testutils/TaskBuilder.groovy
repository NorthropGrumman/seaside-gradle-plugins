package com.ngc.seaside.gradle.testutils

import org.gradle.api.internal.AbstractTask
import org.gradle.api.internal.project.ProjectInternal

import java.util.function.Supplier

/**
 * Used to build tasks for use in tests.  You use this builder because Gradle doesn't allow you to create instances of
 * outside the DSL.  This builder works around that "feature".  The most common way to use it is:
 *
 * <pre>
 *    MyTask task = new TaskBuilder<MyTask>(MyTask).create()
 * </pre>
 *
 * You can specify your own mocked project like this:
 *
 * <pre>
 *     ProjectInternal myMockedProject = GradleMocks.newProjectMock()
 *     MyTask task = new TaskBuilder<MyTask>(MyTask)
 *       .setProject(myMockedProject)
 *       .create()
 * </pre>
 *
 * Finally, you can control the creation of the task with this:
 *
 * <pre>
 *     MyTask task = new TaskBuilder<MyTask>(MyTask)
 *       .setSupplier({ new MyTask() })
 *       .create()
 * </pre>
 *
 * @param <T> the type of task being built
 */
class TaskBuilder<T extends AbstractTask> {

    private final Class<T> taskClazz
    private Supplier<T> taskSupplier
    private String taskName
    private ProjectInternal project

    TaskBuilder(Class<T> taskClazz) {
        this.taskClazz = taskClazz
    }

    TaskBuilder<T> setSupplier(Supplier<T> taskSupplier) {
        this.taskSupplier = taskSupplier
        return this
    }

    TaskBuilder<T> setName(String taskName) {
        this.taskName = taskName
        return this
    }

    TaskBuilder<T> setProject(ProjectInternal project) {
        this.project = project
        return this
    }

    T create() {
        if (taskName == null) {
            taskName = taskClazz.name
        }
        if (project == null) {
            project = GradleMocks.newProjectMock()
        }
        if (taskSupplier == null) {
            taskSupplier = { taskClazz.getConstructor().newInstance() }
        }

        return AbstractTask.injectIntoNewInstance(project,
                                                  taskName,
                                                  taskClazz,
                                                  { taskSupplier.get() })
    }
}

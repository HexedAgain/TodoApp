Need a version of this that is testable, and a version that is not testable, and a way to get from
one to the other - maybe add some features to show the app getting progressively more difficult to test
can start it off without viewmodel (or any real notion of domain layer) and then suggest that as
the beginning of refactoring journey we just go ahead and introduce the viewmodel
Using koin, I can inject directly into the view (which is problematic for previews), as opposed to
injecting into the viewmodel (which can be passed into the root composable by interface)
Could I possibly start it out as an activity with fragments, and an adapter view?
 ** be sure to keep state in the UI ** (mutable state)
maybe have some functions that take reasonably large data classes that we have to populate (show how
passing by interface can make simpler)
maybe define some standalone functions (which are awkward to test)

UI - list of todo items,
each item will have
- a date when the todo was raised,
- a target date for when the todo will be finished
- a title for the todo
- a description of the todo
- an optional dependency on other todos
- a completion percentage (if dependent on other todos)
- a priority (on UI the todos will be draggable)
- some way to show a colour as the todo approaches finish time (i.e. green when todo just started, red for needs completing)

- domain / data:
- todo items will be saved locally to disk (no need for a database)
- maybe some sort of notification mechanism (work manager?) if a todo is approaching completion time
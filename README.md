# interactive-graphs
A javaFX GUI application in Kotlin that allows users to visualize and interact with directed graphs

* The diagram refreshes automatically when the user modifies the graph definition or toggles vertices.
* The application remains responsive, with no UI freezes, even with large graphs.
* Tests are implemented to validate the core logic.

**Core Features**
1. Editable input: users can modify the graph at any time. `A -> B` means there is a directed edge from A to B
2. Diagram display area where the graph is rendered.
3. Vertex list displaying all vertices in the graph. 
   - The vertex list is automatically updated when editing the graph input area. 
   - It gives the ability to enable or disable specific vertices. If a vertex is disabled, it no longer appears in the diagram. 


**Additional Features**:
1. Edges can be labeled. This is useful for showing edge-weights.
2. Loading graphs from text files
3. A few graph examples


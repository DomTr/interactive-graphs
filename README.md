# interactive-graphs
A javaFX GUI application written in Kotlin that allows users to visualize and interact with directed graphs

* **Automatic diagram refresh**: The diagram refreshes automatically when the user modifies the graph definition or toggles vertices.
* **Responsiveness**: The application remains responsive, with no UI freezes, even with large graphs.
* **Core Logic Tests**: Tests are implemented to validate the core logic.

**Core Features**
1. **Editable Graph Input**: users can modify the graph at any time. `A -> B` means there is a directed edge from A to B
2. **Diagram Display**: Graph is rendered in an interactive graph display area. It is possible to navigate/zoom-in or zoom-out. 
3. **Vertex list**:
   - Vertex list displaying all vertices in the graph. 
   - The list is automatically updated when editing the graph input area. 
   - It gives the ability to enable or disable specific vertices. If a vertex is disabled, it no longer appears in the diagram. 


**Additional Features**:
1. **Edge labels**: Edges can be labeled which is useful for indicating edge-weights or other data.
2. **File support**: Load and save graphs in multiple formats, including text, JSON, and GraphM
3. **Graph examples**: Includes a few sample graphs to help users get started.

<img src="https://raw.githubusercontent.com/DomTr/interactive-graphs/main/assets/showcase.gif" alt="Project Demo GIF" width="400"/>

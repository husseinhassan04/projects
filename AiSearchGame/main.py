import pygame as pg
from queue import PriorityQueue as pq

pg.init()

SCREEN_WIDTH = 780
WINDOW = pg.display.set_mode((SCREEN_WIDTH, SCREEN_WIDTH))
pg.display.set_caption("Visualizing Search Algorithms")

DEFAULT = (15, 15, 15)          # Pitch black background  
LINE = (25, 25, 25)             # Darker grid lines (subtle and clean)  
WALL = (90, 90, 90)             # Dark gray walls  
CLOSED = (160, 160, 160)        # Clear medium gray  
OPEN = (100, 0, 150)            # Darker purple (calm and clear)  
START = (0, 180, 255)           # Cyan blue (fresh and cool)  
GOAL = (255, 140, 0)            # Bright orange (warm and visible)  
PATH = (180, 0, 255)            # Brighter purple (clear path)  
BUTTON_COLOR = (25, 25, 25)     # Flat dark buttons  
BUTTON_HOVER = (60, 60, 60)     # Visible hover feedback  
TEXT_COLOR = (245, 245, 245)    # Crisp white text  






FONT = pg.font.SysFont("Arial", 20)

class Node:
    def __init__(self, row, col, width, total_rows):
        self.row = row
        self.col = col
        self.x = row * width
        self.y = col * width
        self.color = DEFAULT
        self.neighbors = []
        self.width = width
        self.total_rows = total_rows

    def get_position(self):
        return self.row, self.col

    def is_wall(self):
        return self.color == WALL

    def is_start(self):
        return self.color == START

    def is_goal(self):
        return self.color == GOAL

    def reset(self):
        self.color = DEFAULT

    def set_wall(self):
        self.color = WALL

    def set_start(self):
        self.color = START

    def set_goal(self):
        self.color = GOAL

    def set_closed(self):
        self.color = CLOSED

    def set_open(self):
        self.color = OPEN

    def set_path(self):
        self.color = PATH

    def draw(self, win):
        pg.draw.rect(win, self.color, (self.x, self.y, self.width, self.width))

    def update_neighbors(self, grid):
        self.neighbors = []
        if self.row < self.total_rows - 1 and not grid[self.row + 1][self.col].is_wall():
            self.neighbors.append(grid[self.row + 1][self.col])
        if self.row > 0 and not grid[self.row - 1][self.col].is_wall():
            self.neighbors.append(grid[self.row - 1][self.col])
        if self.col < self.total_rows - 1 and not grid[self.row][self.col + 1].is_wall():
            self.neighbors.append(grid[self.row][self.col + 1])
        if self.col > 0 and not grid[self.row][self.col - 1].is_wall():
            self.neighbors.append(grid[self.row][self.col - 1])

    def __lt__(self, other):
        return False

def h(p1, p2):
    x1, y1 = p1
    x2, y2 = p2
    return abs(x1 - x2) + abs(y1 - y2)

def reconstruct_path(previous, current, draw):
    while current in previous:
        current = previous[current]
        current.set_path()
        draw()

def a_star(draw_screen, grid, start, goal):

    count = 0
    open_set = pq()  
    open_set.put((0, count, start)) 
    previous ={ }
    g = {node: float("inf") for row in grid for node in row}  
    g[start] = 0
    f = {node: float("inf") for row in grid for node in row}
    f[start] = h(start.get_position(), goal.get_position())

    os_hash = {start}  

    while not open_set.empty():
        for event in pg.event.get():
            if event.type == pg.QUIT:
                pg.quit()

        current = open_set.get()[2]
        os_hash.remove(current)

        if current == goal:
            reconstruct_path(previous, goal, draw_screen)
            goal.set_goal()
            start.set_start()
            print(count)
            return True

        for n in current.neighbors:
            temp_g = g[current] +1
            if temp_g < g[n]:
                previous[n]=current
                g[n]=temp_g
                f[n]=temp_g + h(n.get_position(),goal.get_position())
                if n not in os_hash:
                    count +=1
                    open_set.put((f[n],count,n))
                    os_hash.add(n)
                    n.set_open()
        draw_screen()
        if current != start:
            current.set_closed()
    return False

def best_first_search(draw_screen, grid, start, goal):
    count = 0
    open_set = pq()
    open_set.put((0, count, start))  
    previous = {}
    g = {node: float("inf") for row in grid for node in row}  
    g[start] = 0

    os_hash = {start}

    while not open_set.empty():
        for event in pg.event.get():
            if event.type == pg.QUIT:
                pg.quit()

        current = open_set.get()[2]
        os_hash.remove(current)

        if current == goal:
            reconstruct_path(previous, goal, draw_screen)
            goal.set_goal()
            start.set_start()
            return True

        for n in current.neighbors:
            temp_g = g[current] + 1
            if temp_g < g[n]:
                previous[n] = current
                g[n] = temp_g
                f = h(n.get_position(), goal.get_position())
                if n not in os_hash:
                    count += 1
                    open_set.put((f, count, n))
                    os_hash.add(n)
                    n.set_open()

        draw_screen()
        if current != start:
            current.set_closed()
    return False

def breadth_first_search(draw_screen, grid, start, goal):
    queue = [start]
    visited = {start}
    previous = {}

    while queue:
        for event in pg.event.get():
            if event.type == pg.QUIT:
                pg.quit()

        current = queue.pop(0)

        if current == goal:
            reconstruct_path(previous, goal, draw_screen)
            goal.set_goal()
            start.set_start()
            return True

        for n in current.neighbors:
            if n not in visited:
                visited.add(n)
                previous[n] = current
                queue.append(n)
                n.set_open()

        draw_screen()
        if current != start:
            current.set_closed()

    return False

def create_grid(rows, width):
    grid = []
    gap = width // rows
    for i in range(rows):
        grid.append([])
        for j in range(rows):
            node = Node(i, j, gap, rows)
            grid[i].append(node)
    return grid

def draw_grid(win, rows, width):
    gap = width // rows
    for i in range(rows):
        pg.draw.line(win, LINE, (0, i * gap), (width, i * gap))
        for j in range(rows):
            pg.draw.line(win, LINE, (j * gap, 0), (j * gap, width))

def draw(win, grid, rows, width, buttons):
    win.fill(DEFAULT)

    for row in grid:
        for node in row:
            node.draw(win)

    draw_grid(win, rows, width)

    for button in buttons:
        button.draw(win)

    pg.display.update()

def get_clicked_pos(pos, rows, width):
    gap = width // rows
    y, x = pos
    row = y // gap
    col = x // gap
    return row, col

class Button:
    def __init__(self, x, y, width, height, text, action):
        self.rect = pg.Rect(x, y, width, height)
        self.text = text
        self.action = action

    def draw(self, win):
        mouse_pos = pg.mouse.get_pos()
        color = BUTTON_HOVER if self.rect.collidepoint(mouse_pos) else BUTTON_COLOR
        pg.draw.rect(win, color, self.rect)
        text_surface = FONT.render(self.text, True, TEXT_COLOR)
        win.blit(text_surface, (self.rect.x + (self.rect.width - text_surface.get_width()) // 2,
                                self.rect.y + (self.rect.height - text_surface.get_height()) // 2))

    def click(self, pos):
        return self.rect.collidepoint(pos)

def main(win, width):
    ROWS = 60
    grid = create_grid(ROWS, width)
    start = None
    goal = None

    buttons = [
        Button(10, 10, 120, 30, "Best First", "best"),
        Button(140, 10, 120, 30, "Breadth First", "breadth"),
        Button(270, 10, 120, 30, "A Star", "astar"),
        Button(400, 10, 120, 30,"Reset Search","reset_search"),
        Button(530, 10, 120, 30, "Reset", "reset")
    ]

    run = True
    while run:
        draw(win, grid, ROWS, width, buttons)
        for event in pg.event.get():
            if event.type == pg.QUIT:
                run = False 

        if pg.mouse.get_pressed()[0]:  
            pos = pg.mouse.get_pos()
            for button in buttons:
                if button.click(pos):
                    if button.action == "best" and start and goal:
                        for row in grid:
                            for node in row:
                                node.update_neighbors(grid)
                        best_first_search(lambda: draw(win, grid, ROWS, width, buttons), grid, start, goal)
                    elif button.action == "breadth" and start and goal:
                        for row in grid:
                            for node in row:
                                node.update_neighbors(grid)
                        breadth_first_search(lambda: draw(win, grid, ROWS, width, buttons), grid, start, goal)
                    elif button.action == "astar" and start and goal:
                        for row in grid:
                            for node in row:
                                node.update_neighbors(grid)
                        a_star(lambda: draw(win, grid, ROWS, width, buttons), grid, start, goal)
                    elif button.action == "reset":
                        grid = create_grid(ROWS, width)
                        start, goal = None, None
                    elif button.action == "reset_search":
                        for row in grid:
                            for node in row:
                                if not node.is_start() and not node.is_goal() and not node.is_wall():
                                    node.reset()
                    break
            else:
                row, col = get_clicked_pos(pos, ROWS, width)
                node = grid[row][col]
                if not start and node != goal:
                    start = node
                    start.set_start()
                elif not goal and node != start:
                    goal = node
                    goal.set_goal()
                elif node != start and node != goal:
                    node.set_wall()

        if pg.mouse.get_pressed()[2]:  
                pos = pg.mouse.get_pos()
                row, col = get_clicked_pos(pos, ROWS, width)
                node = grid[row][col]
                node.reset()
                if node == start:
                    start = None
                elif node == goal:
                    goal = None


    pg.quit()

main(WINDOW, SCREEN_WIDTH)

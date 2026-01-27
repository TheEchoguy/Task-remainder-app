const monthNames = [
    "January","February","March","April","May","June",
    "July","August","September","October","November","December"
];

const grid = document.querySelector(".calendar-grid");
const title = document.getElementById("monthTitle");

function renderCalendar() {
    grid.querySelectorAll(".day-cell").forEach(e => e.remove());

    title.textContent = `${monthNames[currentMonth]} ${currentYear}`;

    const firstDay = new Date(currentYear, currentMonth, 1).getDay();
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

    const TOTAL_CELLS = 42;
    let cellCount = 0;

    // Empty cells before month start
    for (let i = 0; i < firstDay; i++) {
        const empty = document.createElement("div");
        empty.className = "day-cell empty-cell";
        grid.appendChild(empty);
        cellCount++;
    }

    // Month days
    for (let day = 1; day <= daysInMonth; day++) {
        const cell = document.createElement("div");
        cell.className = "day-cell";

        const dateStr =
            `${currentYear}-${String(currentMonth + 1).padStart(2,'0')}-${String(day).padStart(2,'0')}`;

        cell.innerHTML = `<div class="day-number">${day}</div>`;

        if (tasksByDate[dateStr]) {
            const tasks = tasksByDate[dateStr];

            // 1–2 tasks → show full info
            if (tasks.length <= 2) {
                tasks.forEach(t => {
                    const priorityClass = (t.priority || "low").toLowerCase();
                    const statusClass =
                        (t.status && t.status.toUpperCase() === "DONE") ? "done" : "pending";

                    const item = document.createElement("div");
                    item.className = `task-title ${priorityClass}`;
                    item.innerHTML = `
                        <span>${t.title}</span>
                        <span class="status-badge ${statusClass}">
                            ${t.status || "PENDING"}
                        </span>
                    `;
                    cell.appendChild(item);
                });
            }

            // 3+ tasks → dots
            else {
                const dots = document.createElement("div");
                dots.className = "dots";

                tasks.forEach(t => {
                    const dot = document.createElement("span");
                    dot.className = `dot ${(t.priority || "low").toLowerCase()}`;
                    dots.appendChild(dot);
                });

                cell.appendChild(dots);
            }

            cell.addEventListener("click", () => showTasks(dateStr));
        }

        grid.appendChild(cell);
        cellCount++;
    }

    // Fill remaining cells
    while (cellCount < TOTAL_CELLS) {
        const empty = document.createElement("div");
        empty.className = "day-cell empty-cell";
        grid.appendChild(empty);
        cellCount++;
    }
}

function prevMonth() {
    currentMonth--;
    if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    }
    renderCalendar();
}

function nextMonth() {
    currentMonth++;
    if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    }
    renderCalendar();
}

function showTasks(date) {
    if (!tasksByDate[date]) return;

    const list = tasksByDate[date]
        .map(t => {
            const status = t.status || "PENDING";
            const priority = t.priority || "LOW";
            const icon = status === "DONE" ? "✔" : "⏳";

            return `${icon} ${t.title}
Priority: ${priority}
Status: ${status}`;
        })
        .join("\n\n");

    alert(`Tasks on ${date}:\n\n${list}`);
}

renderCalendar();

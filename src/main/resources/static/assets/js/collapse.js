document.getElementById("toggleButtons").addEventListener("click", function () {
    let buttons = document.querySelectorAll(".toggle-btn");
    let isCollapsed = this.getAttribute("data-collapsed") === "true";

    buttons.forEach(button => {
        if (isCollapsed) {
            button.innerHTML = button.getAttribute("data-full");
            button.classList.remove("w-[40px]");
            button.classList.add(button.getAttribute("data-width"));
        } else {
            button.innerHTML = button.getAttribute("data-icon");
            button.classList.remove(button.getAttribute("data-width"));
            button.classList.add("w-[40px]");
        }
    });

    this.setAttribute("data-collapsed", isCollapsed ? "false" : "true");
    this.innerHTML = isCollapsed ? '<i class="fas fa-angle-right mr-1"></i> Thu gọn' : '<i class="fas fa-angle-left"></i>';
});

document.addEventListener("DOMContentLoaded", function () {
    const toggleButton = document.getElementById("toggleButtons");
    const actionCells = document.querySelectorAll("td:nth-child(8), th:nth-child(8)");
    let isCollapsed = false;

    toggleButton.addEventListener("click", function () {
        if (!isCollapsed) {
            actionCells.forEach(cell => cell.style.width = "50px");
            toggleButton.innerHTML = '<i class="fas fa-angle-left mr-1"></i>';
        } else {
            actionCells.forEach(cell => cell.style.width = "300px");
            toggleButton.innerHTML = '<i class="fas fa-angle-right mr-1"></i> Thu gọn';
        }
        isCollapsed = !isCollapsed;
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const clearButton = document.getElementById("clearFilters");

    clearButton.addEventListener("click", function () {
        document.querySelectorAll("tr.bg-gray-50 input").forEach(input => {
            input.value = "";
        });

        const rows = document.querySelectorAll("#tableBody tr");
        rows.forEach(row => {
            row.style.display = '';
        });

        updateTotalStores();
    });

    function updateTotalStores() {
        const rows = document.querySelectorAll("#tableBody tr");
        let totalFiltered = 0;

        rows.forEach(row => {
            if (row.style.display !== 'none') {
                totalFiltered++;
            }
        });

        document.querySelector(".total-stores").textContent = `Tổng cộng: ${totalFiltered} cửa hàng`;
    }
});

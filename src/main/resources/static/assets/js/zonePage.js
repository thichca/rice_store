function toggleModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.toggle("hidden");
}
document.addEventListener("DOMContentLoaded", function () {
    flatpickr("#search-date-zonemin", {
        locale: {
            firstDayOfWeek: 1,
            weekdays: {
                shorthand: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
                longhand: ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy']
            },
            months: {
                shorthand: ['Th1', 'Th2', 'Th3', 'Th4', 'Th5', 'Th6', 'Th7', 'Th8', 'Th9', 'Th10', 'Th11', 'Th12'],
                longhand: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
            },
            rangeSeparator: ' đến ',
            time_24hr: true

        },

        enableTime: true,
        dateFormat: "d/m/Y H:i",

    });
    flatpickr("#search-date-zonemax", {
        locale: {
            firstDayOfWeek: 1,
            weekdays: {
                shorthand: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
                longhand: ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy']
            },
            months: {
                shorthand: ['Th1', 'Th2', 'Th3', 'Th4', 'Th5', 'Th6', 'Th7', 'Th8', 'Th9', 'Th10', 'Th11', 'Th12'],
                longhand: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
            },
            rangeSeparator: ' đến ',
            time_24hr: true
        },
        enableTime: true,
        dateFormat: "d/m/Y H:i",
    });
    flatpickr("#search-update-zonemin", {
        locale: {
            firstDayOfWeek: 1,
            weekdays: {
                shorthand: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
                longhand: ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy']
            },
            months: {
                shorthand: ['Th1', 'Th2', 'Th3', 'Th4', 'Th5', 'Th6', 'Th7', 'Th8', 'Th9', 'Th10', 'Th11', 'Th12'],
                longhand: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
            },
            rangeSeparator: ' đến ',
            time_24hr: true
        },
        enableTime: true,
        dateFormat: "d/m/Y H:i",
    });
    flatpickr("#search-update-zonemax", {
        locale: {
            firstDayOfWeek: 1,
            weekdays: {
                shorthand: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
                longhand: ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy']
            },
            months: {
                shorthand: ['Th1', 'Th2', 'Th3', 'Th4', 'Th5', 'Th6', 'Th7', 'Th8', 'Th9', 'Th10', 'Th11', 'Th12'],
                longhand: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
            },
            rangeSeparator: ' đến ',
            time_24hr: true
        },
        enableTime: true,
        dateFormat: "d/m/Y H:i",
    });

    const tableBody = document.getElementById("tableBody");
    const rows = Array.from(tableBody.querySelectorAll("tr" ));
    const recordsPerPageSelect = document.getElementById("recordsPerPage");
    const currentPageInput = document.getElementById("currentPage");
    const totalPagesSpan = document.getElementById("totalPages");
    const prevPageButton = document.getElementById("prevPage");
    const nextPageButton = document.getElementById("nextPage");
    const recordCountSpan = document.getElementById("recordCount");
    const searchInputs = document.querySelectorAll("thead input ");

    let recordsPerPage = parseInt(recordsPerPageSelect.value);
    let currentPage = 1;
    let filteredRows = [...rows]; // Ban đầu danh sách lọc là toàn bộ dữ liệu

    function applyFiltersAndPaginate() {
        let searchIdMin = parseInt(document.getElementById("search-zoneid-min").value.trim()) || null;
        let searchIdMax = parseInt(document.getElementById("search-zoneid-max").value.trim()) || null;
        let searchName = document.getElementById("search-zonename").value.trim().toLowerCase();
        let searchAddress = document.getElementById("search-zoneaddress").value.trim().toLowerCase() ;
        let searchCreatedDateMin = document.getElementById("search-date-zonemin").value.trim();
        let searchCreatedDateMax = document.getElementById("search-date-zonemax").value.trim();
        let searUpdateMin = document.getElementById("search-update-zonemin").value.trim();
        let searchUpdateMax = document.getElementById("search-update-zonemax").value.trim();

        filteredRows = rows.filter(row => {
            let cells = row.getElementsByTagName("td");

            let id = parseInt(cells[0].innerText) || 0;
            let name = cells[1].innerText.trim().toLowerCase();
            let address = cells[2].innerText.trim().toLowerCase();
            let createdDate = new Date(cells[3].innerText.trim());
            let updateDate = new Date(cells[4].innerText.trim());


            let matches = true;

            if (searchIdMin !== null && id < searchIdMin) matches = false;
            if (searchIdMax !== null && id > searchIdMax) matches = false;
            if (searchName && !name.includes(searchName)) matches = false;
            if(searchAddress && !address.includes(searchAddress)) matches = false;
            if (searchCreatedDateMin && createdDate < new Date(searchCreatedDateMin)) matches = false;
            if (searchCreatedDateMax && createdDate > new Date(searchCreatedDateMax)) matches = false;
            if(searUpdateMin && updateDate < new Date(searUpdateMin)) matches = false;
            if(searchUpdateMax && updateDate > new Date(searchUpdateMax)) matches = false;
            return matches;
        });

        // Cập nhật số trang và hiển thị lại dữ liệu
        currentPage = 1;
        updatePagination();
    }

    function updatePagination() {
        let totalPages = Math.max(1, Math.ceil(filteredRows.length / recordsPerPage));
        totalPagesSpan.textContent = ` / ${totalPages}`;
        recordCountSpan.textContent = filteredRows.length;

        // Đảm bảo currentPage không bị vượt quá totalPages
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        // Cập nhật trạng thái của các nút điều hướng
        prevPageButton.disabled = (currentPage <= 1);
        prevPageButton.classList.toggle("opacity-50", currentPage <= 1);
        nextPageButton.disabled = (currentPage >= totalPages);
        nextPageButton.classList.toggle("opacity-50", currentPage >= totalPages);

        displayRows();
    }

    function displayRows() {
        tableBody.innerHTML = "";

        let start = (currentPage - 1) * recordsPerPage;
        let end = start + recordsPerPage;

        filteredRows.slice(start, end).forEach(row => {
            tableBody.appendChild(row);
        });

        currentPageInput.value = currentPage;
    }

    prevPageButton.addEventListener("click", function () {
        if (currentPage > 1) {
            currentPage--;
            updatePagination(); // Gọi hàm cập nhật số trang và hiển thị lại dữ liệu
        }
    });

    nextPageButton.addEventListener("click", function () {
        let totalPages = Math.max(1, Math.ceil(filteredRows.length / recordsPerPage));
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination(); // Gọi hàm cập nhật số trang
        }
    });

    currentPageInput.addEventListener("change", function () {
        let totalPages = Math.max(1, Math.ceil(filteredRows.length / recordsPerPage));
        let newPage = parseInt(this.value);
        if (!isNaN(newPage) && newPage >= 1 && newPage <= totalPages) {
            currentPage = newPage;
        } else {
            currentPage = 1;
        }
        updatePagination();
    });

    recordsPerPageSelect.addEventListener("change", function () {
        recordsPerPage = parseInt(this.value);
        currentPage = 1;
        updatePagination();
    });

    searchInputs.forEach(input => {
        input.addEventListener("input", applyFiltersAndPaginate);
    });

    document.getElementById("clearFilters").addEventListener("click", function () {
        searchInputs.forEach(input => input.value = "");
        applyFiltersAndPaginate();
    });

    applyFiltersAndPaginate();
});
s
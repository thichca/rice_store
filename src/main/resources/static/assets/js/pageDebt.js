function toggleModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.toggle("hidden");
}

document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("tableBody");
   // const inputs = document.querySelectorAll("input , select");
    const rows = Array.from(tableBody.querySelectorAll("tr" ));
    const recordsPerPageSelect = document.getElementById("recordsPerPage");
    const currentPageInput = document.getElementById("currentPage");
    const totalPagesSpan = document.getElementById("totalPages");
    const prevPageButton = document.getElementById("prevPage");
    const nextPageButton = document.getElementById("nextPage");
    const recordCountSpan = document.getElementById("recordCount");
    const searchInputs = document.querySelectorAll("thead input");

    let recordsPerPage = parseInt(recordsPerPageSelect.value);
    let currentPage = 1;
    let filteredRows = [...rows]; // Ban đầu danh sách lọc là toàn bộ dữ liệu

    function applyFiltersAndPaginate() {
        let searchIdMin = parseInt(document.getElementById("search-id-min").value.trim()) || null;
        let searchIdMax = parseInt(document.getElementById("search-id-max").value.trim()) || null;
        let searchNote = document.getElementById("search-note").value.trim().toLowerCase();
        let searchType = document.getElementById("search-type").value.trim() ;
        let searchDebtMin = parseFloat(document.getElementById("search-debt-min").value.trim()) || null;
        let searchDebtMax = parseFloat(document.getElementById("search-debt-max").value.trim()) || null;
        let searchCreatedDateMin = document.getElementById("search-date-min").value.trim();
        let searchCreatedDateMax = document.getElementById("search-date-max").value.trim();


        filteredRows = rows.filter(row => {
            let cells = row.getElementsByTagName("td");

            let id = parseInt(cells[0].innerText) || 0;
            let note = cells[1].innerText.trim().toLowerCase();
            let type = cells[2].innerText.trim().toLowerCase();
            let amount = cells[3].innerText.trim().toLowerCase();
            let createdDate = new Date(cells[4].innerText.trim());


            let matches = true;

            if (searchIdMin !== null && id < searchIdMin) matches = false;
            if (searchIdMax !== null && id > searchIdMax) matches = false;
            if (searchNote && !note.includes(searchNote)) matches = false;
            if (searchType && type.toLowerCase() !== searchType.toLowerCase()) matches = false;


            if (searchDebtMin !== null && amount < searchDebtMin) matches = false;
            if (searchDebtMax !== null && amount > searchDebtMax) matches = false;
            if (searchCreatedDateMin && createdDate < new Date(searchCreatedDateMin)) matches = false;
            if (searchCreatedDateMax && createdDate > new Date(searchCreatedDateMax)) matches = false;
            // if (searchUpdatedDateMin && updatedDate < new Date(searchUpdatedDateMin)) matches = false;
            // if (searchUpdatedDateMax && updatedDate > new Date(searchUpdatedDateMax)) matches = false;

            return matches;
        });

        // Cập nhật số trang và hiển thị lại dữ liệu
        currentPage = 1;
        updatePagination();
    }
    document.getElementById("search-type").addEventListener("change", function () {
        applyFiltersAndPaginate(); // Gọi lại hàm lọc ngay khi thay đổi loại nợ
    });
    document.getElementById("clearFilters").addEventListener("click", function () {
        document.getElementById("search-type").value = ""; // Xóa giá trị loại nợ
        applyFiltersAndPaginate(); // Gọi lại hàm lọc để lấy danh sách đầy đủ
    });


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

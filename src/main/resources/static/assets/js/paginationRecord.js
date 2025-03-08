function toggleModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.toggle("hidden");
}

document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("tableBody");
    const rows = Array.from(tableBody.querySelectorAll("tr"));
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
        let searchName = document.getElementById("search-name").value.trim().toLowerCase();
        let searchPhone = document.getElementById("search-phone").value.trim().toLowerCase();
        let searchAddress = document.getElementById("search-address").value.trim().toLowerCase();
        let searchEmail = document.getElementById("search-email").value.trim().toLowerCase();
        let searchDebtMin = parseFloat(document.getElementById("search-debt-min").value.trim()) || null;
        let searchDebtMax = parseFloat(document.getElementById("search-debt-max").value.trim()) || null;
        let searchCreatedby = document.getElementById("search-createdby").value.trim().toLowerCase();
        let searchUpdatedby= document.getElementById("search-updateby").value.trim().toLowerCase();


        filteredRows = rows.filter(row => {
            let cells = row.getElementsByTagName("td");

            let id = parseInt(cells[0].innerText) || 0;
            let name = cells[1].innerText.trim().toLowerCase();
            let address = cells[2].innerText.trim().toLowerCase();
            let phone = cells[3].innerText.trim().toLowerCase();
            let email = cells[4].innerText.trim().toLowerCase();
            let debt = parseFloat(cells[5].innerText) || 0;
            let createdby = cells[6].innerText.trim().toLowerCase();
            let updatedby = cells[7].innerText.trim().toLowerCase();


            let matches = true;

            if (searchIdMin !== null && id < searchIdMin) matches = false;
            if (searchIdMax !== null && id > searchIdMax) matches = false;
            if (searchName && !name.includes(searchName)) matches = false;
            if (searchPhone && !phone.includes(searchPhone)) matches = false;
            if (searchAddress && !address.includes(searchAddress)) matches = false;
            if (searchEmail && !email.includes(searchEmail)) matches = false;
            if (searchDebtMin !== null && debt < searchDebtMin) matches = false;
            if (searchDebtMax !== null && debt > searchDebtMax) matches = false;
            if (searchCreatedby && !createdby.includes(searchCreatedby)) matches = false;
            if (searchUpdatedby && !updatedby.includes(searchUpdatedby)) matches = false;


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
async function submitCustomerForm(event) {
    event.preventDefault();  // Ngăn form reload trang

    let form = event.target;
    let formData = new FormData(form);

    let response = await fetch(form.action, {
        method: form.method,
        body: formData
    });

    let result = await response.json();

    if (!response.ok) {
        let errorMessage = "<ul>";
        if (result.errorMessage) {
            errorMessage += `<li>${result.errorMessage}</li>`;
        } else {
            for (const [field, message] of Object.entries(result)) {
                errorMessage += `<li>${message}</li>`;
            }
        }
        errorMessage += "</ul>";

        document.getElementById("errorModalContent").innerHTML = errorMessage;
        toggleModal("errorModal");
    } else {

        location.reload();
    }
}

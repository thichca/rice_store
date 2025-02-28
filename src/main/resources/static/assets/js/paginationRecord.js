document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("tableBody");
    const rows = Array.from(tableBody.querySelectorAll("tr")); // Lấy tất cả các hàng dữ liệu
    const recordsPerPageSelect = document.getElementById("recordsPerPage");
    const currentPageInput = document.getElementById("currentPage");
    const totalPagesSpan = document.getElementById("totalPages");
    const prevPageButton = document.getElementById("prevPage");
    const nextPageButton = document.getElementById("nextPage");

    let recordsPerPage = parseInt(recordsPerPageSelect.value);
    let currentPage = 1;
    let totalPages = Math.ceil(rows.length / recordsPerPage);

    function updatePagination() {
        totalPages = Math.max(1, Math.ceil(rows.length / recordsPerPage)); // Đảm bảo ít nhất có 1 trang
        totalPagesSpan.textContent = ` / ${totalPages}`;

        // Cập nhật danh sách hiển thị
        displayRows();

        // Vô hiệu hóa nút "Trước" nếu đang ở trang đầu
        prevPageButton.disabled = (currentPage === 1);
        prevPageButton.classList.toggle("opacity-50", currentPage === 1);

        // Vô hiệu hóa nút "Sau" nếu đang ở trang cuối
        nextPageButton.disabled = (currentPage === totalPages);
        nextPageButton.classList.toggle("opacity-50", currentPage === totalPages);
    }

    function displayRows() {
        tableBody.innerHTML = ""; // Xóa toàn bộ nội dung hiện tại

        let start = (currentPage - 1) * recordsPerPage;
        let end = start + recordsPerPage;

        rows.slice(start, end).forEach(row => {
            tableBody.appendChild(row);
        });

        currentPageInput.value = currentPage;
    }

    // Sự kiện khi chọn số bản ghi mỗi trang
    recordsPerPageSelect.addEventListener("change", function () {
        recordsPerPage = parseInt(this.value);
        currentPage = 1; // Reset về trang đầu tiên
        updatePagination();
    });

    // Sự kiện khi nhấn nút "Trước"
    prevPageButton.addEventListener("click", function () {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    });

    // Sự kiện khi nhấn nút "Sau"
    nextPageButton.addEventListener("click", function () {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    });

    // Sự kiện khi nhập số trang
    currentPageInput.addEventListener("change", function () {
        let newPage = parseInt(this.value);
        if (!isNaN(newPage) && newPage >= 1 && newPage <= totalPages) {
            currentPage = newPage;
        } else {
            currentPage = 1; // Reset về trang đầu nếu nhập sai
        }
        updatePagination();
    });

    // Khởi tạo phân trang
    updatePagination();
});

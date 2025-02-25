document.addEventListener("DOMContentLoaded", function () {
    let inputs = document.querySelectorAll("input");
    let rows = Array.from(document.querySelectorAll("#customerTable tbody tr"));
    let pageSize = 3; // Số khách hàng hiển thị trên mỗi trang
    let currentPage = 0;

    // Thêm bộ lọc cho trạng thái nhân viên
    let searchStatus = document.getElementById("search-status");

    function applyFiltersAndPaginate() {
        let searchIdMin = parseInt(document.getElementById("search-id-min").value.trim()) || null;
        let searchName = document.getElementById("search-name").value.trim().toLowerCase();
        let searchPhone = document.getElementById("search-phone").value.trim().toLowerCase();
        let searchAddress = document.getElementById("search-address").value.trim().toLowerCase();
        let selectedStatus = searchStatus.value; // Lấy trạng thái từ dropdown

        let filteredRows = rows.filter(row => {
            let cells = row.getElementsByTagName("td");

            let id = parseInt(cells[0].innerText) || 0;
            let name = cells[1].innerText.trim().toLowerCase();
            let phone = cells[2].innerText.trim().toLowerCase();
            let address = cells[3].innerText.trim().toLowerCase();
            let status = cells[4].innerText.trim().toLowerCase(); // Trạng thái nhân viên (có thể là "Đang làm việc" hoặc "Đã nghỉ")

            let matches = true;

            // Kiểm tra ID
            if (searchIdMin !== null && id < searchIdMin) matches = false;

            // Kiểm tra Tên, SĐT, Địa chỉ
            if (searchName && !name.includes(searchName)) matches = false;
            if (searchPhone && !phone.includes(searchPhone)) matches = false;
            if (searchAddress && !address.includes(searchAddress)) matches = false;

            // Kiểm tra trạng thái (Nếu có chọn trạng thái từ dropdown)
            if (selectedStatus === "working" && status !== "đang làm việc") {
                matches = false;
            }
            if (selectedStatus === "inactive" && status !== "đã nghỉ") {
                matches = false;
            }

            return matches;
        });

        paginate(filteredRows);
    }

    function paginate(filteredRows) {
        let totalPages = Math.ceil(filteredRows.length / pageSize);
        let start = currentPage * pageSize;
        let end = start + pageSize;

        // Ẩn tất cả hàng
        rows.forEach(row => row.style.display = "none");

        // Hiển thị hàng phù hợp với bộ lọc
        filteredRows.slice(start, end).forEach(row => row.style.display = "");

        // Cập nhật phân trang
        updatePagination(totalPages);
    }

    function updatePagination(totalPages) {
        let paginationDiv = document.getElementById("pagination");
        paginationDiv.innerHTML = "";

        const maxButtons = 4;

        if (totalPages <= maxButtons) {
            for (let i = 0; i < totalPages; i++) {
                let pageButton = document.createElement("button");
                pageButton.classList.add("btn", "btn-primary", "me-2");
                pageButton.innerText = i + 1;
                if (currentPage === i) {
                    pageButton.classList.add("active");
                    pageButton.disabled = true;
                }
                pageButton.addEventListener("click", function () {
                    currentPage = i;
                    applyFiltersAndPaginate();
                });
                paginationDiv.appendChild(pageButton);
            }
            return;
        }

        let startPage, endPage;
        let middleButtons = maxButtons - 2;

        startPage = currentPage - Math.floor(middleButtons / 2);
        endPage = currentPage + Math.floor(middleButtons / 2);

        if (startPage < 1) {
            startPage = 1;
            endPage = startPage + middleButtons - 1;
        }
        if (endPage > totalPages - 2) {
            endPage = totalPages - 2;
            startPage = endPage - middleButtons + 1;
        }

        let firstPageButton = document.createElement("button");
        firstPageButton.classList.add("btn", "btn-primary", "me-2");
        firstPageButton.innerText = "1";
        if (currentPage === 0) {
            firstPageButton.classList.add("active");
            firstPageButton.disabled = true;
        }
        firstPageButton.addEventListener("click", function () {
            currentPage = 0;
            applyFiltersAndPaginate();
        });
        paginationDiv.appendChild(firstPageButton);

        if (startPage > 1) {
            let ellipsis = document.createElement("span");
            ellipsis.innerText = "...";
            ellipsis.classList.add("btn", "btn-light", "disabled", "me-2");
            paginationDiv.appendChild(ellipsis);
        }

        for (let i = startPage; i <= endPage; i++) {
            let pageButton = document.createElement("button");
            pageButton.classList.add("btn", "btn-primary", "me-2");
            pageButton.innerText = i + 1;
            if (currentPage === i) {
                pageButton.classList.add("active");
                pageButton.disabled = true;
            }
            pageButton.addEventListener("click", function () {
                currentPage = i;
                applyFiltersAndPaginate();
            });
            paginationDiv.appendChild(pageButton);
        }

        if (endPage < totalPages - 2) {
            let ellipsis2 = document.createElement("span");
            ellipsis2.innerText = "...";
            ellipsis2.classList.add("btn", "btn-light", "disabled", "me-2");
            paginationDiv.appendChild(ellipsis2);
        }

        let lastPageButton = document.createElement("button");
        lastPageButton.classList.add("btn", "btn-primary", "me-2");
        lastPageButton.innerText = totalPages;
        if (currentPage === totalPages - 1) {
            lastPageButton.classList.add("active");
            lastPageButton.disabled = true;
        }
        lastPageButton.addEventListener("click", function () {
            currentPage = totalPages - 1;
            applyFiltersAndPaginate();
        });
        paginationDiv.appendChild(lastPageButton);
    }

    // Gán sự kiện cho các input và dropdown
    inputs.forEach(input => {
        input.addEventListener("input", function () {
            currentPage = 0;
            applyFiltersAndPaginate();
        });
    });

    // Gán sự kiện cho dropdown trạng thái
    searchStatus.addEventListener("change", function () {
        currentPage = 0;
        applyFiltersAndPaginate();
    });

    // Xóa bộ lọc
    document.getElementById("clearFilter").addEventListener("click", function () {
        inputs.forEach(input => input.value = "");
        searchStatus.value = "working"; // Reset trạng thái về mặc định "Đang làm việc"
        currentPage = 0;
        applyFiltersAndPaginate();
    });

    // Khởi chạy bộ lọc khi tải trang
    applyFiltersAndPaginate();
});




// Get elements
// var showFormButton = document.getElementById("showFormButton");
// var overlay = document.getElementById("overlay");
// var cancelButton = document.getElementById("cancelButton");
//
// // When the user clicks the "Nhân viên" button, show the overlay with form
// showFormButton.onclick = function () {
//     overlay.style.display = "flex"; // Show overlay (centered form)
// }
//
// // When the user clicks "Bỏ qua", hide the overlay with form
// cancelButton.onclick = function () {
//     overlay.style.display = "none"; // Hide overlay
// }
// overlay.addEventListener("click", function (event) {
//     if (event.target === overlay) {
//         overlay.style.display = "none";
//     }
// });
// Lấy các phần tử cần thiết
var overlay1 = document.getElementById("overlay1");
var cancelButton1 = document.getElementById("cancelButton1");

// Gán sự kiện click cho tất cả nút "Sửa" khi trang được load
document.querySelectorAll(".btn-success").forEach(function (button) {
    button.addEventListener("click", function () {
        // Lấy hàng chứa nút "Sửa" được bấm
        var row = this.closest("tr");

        // Lấy dữ liệu của nhân viên từ hàng đó
        var employeeId = row.cells[0].textContent;
        var username = row.cells[1].textContent;
        var email = row.cells[2].textContent;
        var phone = row.cells[3].textContent;

        console.log("ID nhân viên:", employeeId);

        // Gán giá trị vào form trong overlay
        document.querySelector("#overlay1 input[name='username']").value = username;
        document.querySelector("#overlay1 input[name='email']").value = email;
        document.querySelector("#overlay1 input[name='phone']").value = phone;

        // Hiển thị overlay với form sửa
        overlay1.style.display = "flex";
    });
});

// Khi bấm "Bỏ qua", ẩn overlay đi
cancelButton1.onclick = function () {
    overlay1.style.display = "none";
};

// Ẩn overlay khi bấm bên ngoài form
overlay1.addEventListener("click", function (event) {
    if (event.target === overlay1) {
        overlay1.style.display = "none";
    }
});

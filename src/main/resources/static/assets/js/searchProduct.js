document.getElementById("search-input").addEventListener("input", function () {
    const searchQuery = document.getElementById("search-input").value;
    if (searchQuery.trim() === "") {
        document.getElementById("suggestions-list").classList.add("hidden");
    } else {
        fetch(`/api/products/search?query=${searchQuery}`)
            .then(response => response.json())
            .then(data => {
                displaySuggestions(data);
            })
            .catch(error => console.error('Error:', error));
    }
});

// Ẩn danh sách gợi ý khi ô tìm kiếm mất focus
document.getElementById("search-input").addEventListener("blur", function () {
    setTimeout(() => {
        document.getElementById("suggestions-list").classList.add("hidden");
    }, 200);
});

// Hiển thị danh sách gợi ý khi tìm kiếm sản phẩm
function displaySuggestions(data) {
    const suggestionsList = document.getElementById("suggestions-list");
    suggestionsList.innerHTML = "";

    if (data.length === 0) {
        suggestionsList.classList.add("hidden");
        return;
    }

    // Tạo bảng hiển thị sản phẩm
    const table = document.createElement("table");
    table.classList.add("w-full", "border-collapse", "table-auto");

    // Tạo phần tiêu đề bảng
    const headerRow = document.createElement("tr");
    headerRow.innerHTML = `
        <th class="px-4 py-2 border">Tên sản phẩm</th>
        <th class="px-4 py-2 border">Khu vực</th>
        <th class="px-4 py-2 border">Giá (VNĐ)</th>
        <th class="px-4 py-2 border">Số lượng</th>
    `;
    table.appendChild(headerRow);

    // Duyệt qua danh sách sản phẩm và thêm vào bảng
    data.forEach(item => {
        const row = document.createElement("tr");
        row.classList.add("hover:bg-gray-100");

        row.innerHTML = `
            <td class="px-4 py-2 border">${item.productName}</td>
            <td class="px-4 py-2 border text-center">${item.zoneName}</td>
            <td class="px-4 py-2 border text-center">${item.price} đ</td>
            <td class="px-4 py-2 border text-center">${item.quantity}</td>

        `;

        row.dataset.maxQuantity = item.quantity;

        // Khi click vào sản phẩm, thêm vào danh sách "Sản phẩm đã chọn"
        row.addEventListener("click", function () {
            addProductToSelected(item);
            suggestionsList.classList.add("hidden");
        });
        table.appendChild(row);
    });

    suggestionsList.appendChild(table);
    suggestionsList.classList.remove("hidden");
}

// Thêm sản phẩm vào danh sách "Sản phẩm đã chọn"
function addProductToSelected(product) {
    const selectedProductsTableBody = document.querySelector("#selected-products");

    const row = document.createElement("tr");
    row.classList.add("hover:bg-gray-100");

    row.dataset.productId = product.productId;
    row.dataset.zoneId = product.zoneId;
    row.dataset.maxQuantity = product.quantity;

    row.innerHTML = `
        <td class="py-2 px-4 border-b text-center">${selectedProductsTableBody.rows.length + 1}</td>
        <td class="py-2 px-4 border-b">${product.productName}</td>
        <td class="py-2 px-4 border-b text-center">${product.zoneName}</td>
        <td class="py-2 px-4 border-b text-center">${product.price} đ</td>
        <td class="py-2 px-4 border-b text-center">
            <input class="w-12 text-center rounded border-gray-300 focus:border-blue-500 outline-none transition duration-300" type="number" value="1" min="1" max="${product.quantity}"
                oninput="updateOrderDetails()"/>
        </td>
        <td class="py-2 px-4 border-b text-center">
            <button class="bg-red-500 text-white px-2 py-1 ml-2 rounded" onclick="removeProduct(this)">
                <i class="fas fa-trash text-sm"></i>
            </button>
        </td>
    `;

    selectedProductsTableBody.appendChild(row);
    updateOrderDetails(); // Cập nhật ngay chi tiết đơn hàng
}

// Hàm xóa sản phẩm và cập nhật lại STT
function removeProduct(button) {
    const row = button.closest("tr");
    row.remove(); // Xóa sản phẩm khỏi danh sách

    // Cập nhật lại STT sau khi xóa sản phẩm
    updateSTT();

    // Cập nhật lại chi tiết đơn hàng
    updateOrderDetails();
}

// Hàm cập nhật lại STT của tất cả sản phẩm trong danh sách "Sản phẩm đã chọn"
function updateSTT() {
    const selectedProductsTableBody = document.querySelector("#selected-products");
    let rows = selectedProductsTableBody.querySelectorAll("tr");

    rows.forEach((row, index) => {
        row.cells[0].textContent = index + 1; // Cập nhật lại STT theo thứ tự đúng
    });
}

// Cập nhật chi tiết đơn hàng
function updateOrderDetails() {
    const orderDetailsTableBody = document.querySelector("#order-details");
    orderDetailsTableBody.innerHTML = ""; // Xóa bảng cũ

    let totalAmount = 0;
    const selectedProductsTableBody = document.querySelector("#selected-products");

    selectedProductsTableBody.querySelectorAll("tr").forEach(row => {
        const productName = row.cells[1].textContent;
        const productZone = row.cells[2].textContent;
        let productPrice = parseInt(row.cells[3].textContent.replace(" đ", ""));
        let quantity = parseInt(row.querySelector("input").value);
        const maxQuantity = parseInt(row.dataset.maxQuantity); // Lấy số lượng tối đa từ thuộc tính dữ liệu

        // Kiểm tra và xử lý nếu số lượng không phải là số nguyên dương hợp lệ hoặc vượt quá số lượng tối đa
        if (isNaN(quantity) || quantity < 1) {
            quantity = 1;  // Đặt lại về 1 nếu giá trị không hợp lệ
            row.querySelector("input").value = quantity;
        }

        if (quantity > maxQuantity) {
            quantity = maxQuantity;  // Nếu số lượng vượt quá số lượng tối đa, đặt lại bằng maxQuantity
            row.querySelector("input").value = quantity;

            alert(`Số lượng của sản phẩm "${productName}" chỉ còn ${maxQuantity}`);
        }

        const total = productPrice * quantity;

        totalAmount += total;

        // Thêm sản phẩm vào bảng "Chi tiết đơn hàng"
        const detailRow = document.createElement("tr");
        detailRow.innerHTML = `
            <td class="py-2 px-4 border-b text-center">${orderDetailsTableBody.rows.length + 1}</td>
            <td class="py-2 px-4 border-b">${productName}</td>
            <td class="py-2 px-4 border-b text-center">
                <input class="w-12 text-center rounded border-gray-300 focus:border-blue-500 outline-none transition duration-300" type="number" value="${quantity}" min="1" max="${maxQuantity}"
                    oninput="updateOrderDetails()"/>
            </td>
            <td class="py-2 px-4 border-b text-center">${productPrice} đ</td>
            <td class="py-2 px-4 border-b text-center total-price">${total} đ</td>
        `;
        orderDetailsTableBody.appendChild(detailRow);
    });

    // Cập nhật tổng số tiền
    document.querySelector("#total-amount").textContent = totalAmount + " đ";
    updateTotalAmount();
}

// Cập nhật tổng tiền và giảm giá
function updateTotalAmount() {
    const discountInput = document.getElementById("discount-input");
    let discountAmount = parseInt(discountInput.value) || 0;

    // Lấy tổng số tiền
    let totalAmount = 0;
    const selectedProductsTableBody = document.querySelector("#selected-products");

    selectedProductsTableBody.querySelectorAll("tr").forEach(row => {
        const productPrice = parseInt(row.cells[3].textContent.replace(" đ", ""));
        const quantity = parseInt(row.querySelector("input").value);
        const total = productPrice * quantity;
        totalAmount += total;
    });

    // Kiểm tra và điều chỉnh giá trị giảm giá
    const maxDiscount = totalAmount * 0.05;
    if (discountAmount < 0) {
        discountAmount = 0;
    } else if (discountAmount > maxDiscount) {
        discountAmount = maxDiscount;
        alert(`Giảm giá tối đa chỉ được ${maxDiscount} đ (5% tổng giá trị đơn hàng).`);
    }

    totalAmount -= discountAmount;
    document.querySelector("#discount-amount").textContent = discountAmount + " đ";
    document.querySelector("#total-amount").textContent = totalAmount + " đ";

    document.getElementById("total-amount-hidden").value = totalAmount;
}

function prepareOrderData(event) {
    event.preventDefault(); // Ngăn form gửi ngay lập tức

    const selectedProductsTableBody = document.querySelector("#selected-products");
    let selectedProducts = [];

    selectedProductsTableBody.querySelectorAll("tr").forEach(row => {
        const productId = row.dataset.productId; // Cần thêm thuộc tính data-product-id vào hàng sản phẩm khi tạo
        const zoneId = row.dataset.zoneId;       // Cần thêm thuộc tính data-zone-id vào hàng sản phẩm khi tạo
        const quantity = parseInt(row.querySelector("input").value);

        if (productId && zoneId && quantity > 0) {
            selectedProducts.push({productId, zoneId, quantity});
        }
    });

    document.getElementById("selected-products-hidden").value = JSON.stringify(selectedProducts);

    // Gửi form sau khi cập nhật dữ liệu
    event.target.submit();
}

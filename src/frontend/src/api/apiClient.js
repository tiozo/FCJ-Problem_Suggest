// frontend/src/api/apiClient.js
import axios from 'axios';

// Tạo một instance của axios với cấu hình mặc định
// Đây là cách làm rất chuyên nghiệp để quản lý kết nối API
const apiClient = axios.create({
    // baseURL sẽ được thêm vào trước tất cả các request.
    // Chúng ta sẽ dùng proxy của Vite để chuyển hướng '/api' đến backend Java.
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

/**
 * Hàm để upload file lên backend Spring Boot.
 * @param {File} file - Đối tượng file được chọn từ input.
 * @param {Function} onUploadProgress - Callback để theo dõi tiến trình upload.
 * @returns {Promise<any>} - Promise chứa phản hồi từ server.*/
export const uploadFile = (file, onUploadProgress) => {
    // FormData là cách tiêu chuẩn để gửi file qua HTTP.
    const formData = new FormData();

    // Key 'file' ở đây PHẢI KHỚP với @RequestParam("file") trong controller Java của bạn.
    formData.append('file', file);

    // Thực hiện request POST để upload file
    return apiClient.post('/ver0.0.1/file-receiver/upload', formData, {
        headers: {
            // Đối với FormData, trình duyệt sẽ tự động đặt Content-Type đúng
            // bao gồm cả boundary, nên chúng ta không cần ghi đè ở đây.
            'Content-Type': 'multipart/form-data',
        },
        // Callback này của axios sẽ được gọi liên tục trong quá trình upload
        onUploadProgress,
    });
};

// Bạn có thể thêm các hàm API khác ở đây trong tương lai
// Ví dụ: export const getStatements = () => apiClient.get('/statements');

export default apiClient;
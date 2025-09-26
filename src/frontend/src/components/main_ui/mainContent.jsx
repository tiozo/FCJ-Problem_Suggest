// frontend/src/pages/UploadPage.jsx
import { uploadFile } from '../../api/apiClient';
import React, { useState } from 'react';
import UploadPanel from './uploadPane';
import '../styles/mainContent.css';

const UploadBarIcon = () => <svg viewBox="0 0 24 24" fill="none"><path d="M5 20.5h14v-2H5v2zm0-10h4v6h6v-6h4l-7-7-7 7z" fill="currentColor"></path></svg>;
const FileIcon = () => <svg viewBox="0 0 24 24"><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path><polyline points="13 2 13 9 20 9"></polyline></svg>;

const UploadPage = () => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [isPanelOpen, setIsPanelOpen] = useState(false);
    const [statusMessage, setStatusMessage] = useState('');
    const [uploadProgress, setUploadProgress] = useState(0); // State mới để lưu tiến trình

    const handleFileSelect = (file) => {
        setSelectedFile(file);
        setIsPanelOpen(false);
        setStatusMessage('');
        setUploadProgress(0); // Reset tiến trình khi chọn file mới
    };

    const handleProcessFile = async () => {
        if (!selectedFile) {
            setStatusMessage('Vui lòng chọn một file để xử lý.');
            return;
        }

        setStatusMessage(`Đang tải lên file: ${selectedFile.name}...`);

        try {
            // Sử dụng hàm uploadFile từ apiClient
            const response = await uploadFile(selectedFile, (progressEvent) => {
                const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                setUploadProgress(percentCompleted); // Cập nhật tiến trình
            });

            console.log('Phản hồi từ server:', response.data);
            setStatusMessage('Tải lên thành công! Bắt đầu xử lý trên backend...');

        } catch (error) {
            console.error('Lỗi khi tải file:', error);
            setStatusMessage('Lỗi khi tải file. Vui lòng kiểm tra console.');
            setUploadProgress(0); // Reset tiến trình khi có lỗi
        }
    };

    return (
        <main className="upload-page-container">
            <div className="upload-content">

                <div className="title-area">
                    <h1 className="main-title">PDF Statement Tagger</h1>
                </div>

                <input
                    type="text"
                    className="upload-bar-input"
                    placeholder="Nhập ghi chú hoặc mô tả cho file (tùy chọn)..."
                />

                {selectedFile && (
                    <div className="selected-file-display">
                        <FileIcon />
                        <span className="file-name">{selectedFile.name}</span>
                        <button
                            className="clear-file-btn"
                            onClick={() => setSelectedFile(null)}
                            title="Chọn file khác"
                        >
                            &times;
                        </button>
                    </div>
                )}

                <nav className="actions-bar">
                    {/* Nút này bây giờ sẽ kích hoạt việc xử lý file */}
                    <button className="action-btn process" onClick={handleProcessFile}>
                        Process Document
                    </button>
                    <div className="actions-spacer"></div>
                    <button className="action-btn active" onClick={() => setIsPanelOpen(!isPanelOpen)}>
                        <UploadBarIcon />
                        Upload
                    </button>
                </nav>

                {/* Hiển thị thông báo trạng thái */}
                {statusMessage && <p className="status-message">{statusMessage}</p>}

                {isPanelOpen &&
                    <UploadPanel
                        onFileSelect={handleFileSelect}
                        onClose={() => setIsPanelOpen(false)}
                    />
                }
            </div>
        </main>
    );
};

export default UploadPage;
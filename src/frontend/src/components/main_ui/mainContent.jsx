// frontend/src/pages/UploadPage.jsx
import React, { useState } from 'react';
import UploadPanel from './uploadPane';
import '../styles/mainContent.css';

const UploadBarIcon = () => <svg viewBox="0 0 24 24" fill="none"><path d="M5 20.5h14v-2H5v2zm0-10h4v6h6v-6h4l-7-7-7 7z" fill="currentColor"></path></svg>;
const FileIcon = () => <svg viewBox="0 0 24 24"><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path><polyline points="13 2 13 9 20 9"></polyline></svg>;

const UploadPage = () => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [isPanelOpen, setIsPanelOpen] = useState(false);

    const handleFileSelect = (file) => {
        setSelectedFile(file);
        setIsPanelOpen(false);
    };

    return (
        <main className="upload-page-container">
            <div className="upload-content">

                <div className="title-area">
                    <h1 className="main-title">PDF Statement Tagger</h1>
                </div>

                {/* --- Thay đổi Bắt đầu --- */}
                {/* Thanh Upload chính bây giờ là một thẻ input thực sự */}
                <input
                    type="text"
                    className="upload-bar-input"
                    placeholder="Enter what you want to process or know about"
                />

                {/* Khu vực mới để hiển thị file đã chọn */}
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
                {/* --- Thay đổi Kết thúc --- */}

                {/* Khu vực các nút bấm bên dưới thanh upload */}
                <nav className="actions-bar">
                    <button className="action-btn">Natural Language</button>
                    <button className="action-btn">Math Input</button>
                    <div className="actions-spacer"></div>
                    <button className="action-btn">Extended Keyboard</button>
                    <button className="action-btn">Examples</button>
                    <button className="action-btn active" onClick={() => setIsPanelOpen(!isPanelOpen)}>
                        <UploadBarIcon />
                        Upload
                    </button>
                    <button className="action-btn">Random</button>
                </nav>

                {/* Hiển thị UploadPanel có điều kiện */}
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
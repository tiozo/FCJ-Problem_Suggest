// frontend/src/components/UploadPanel.jsx
import React, { useState } from 'react';
import '../styles/uploadPane.css';

// Thay đổi 1: Định nghĩa component icon mới (FileInputIcon) với SVG bạn cung cấp
const FileInputIcon = () => (
    <svg viewBox="0 0 24 24">
        <path d="M14.59 2.59c-.38-.38-.89-.59-1.42-.59H6c-1.1 0-2 .9-2 2v16c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8.83c0-.53-.21-1.04-.59-1.41l-4.82-4.83zM15 16h-2v2c0 .55-.45 1-1 1s-1-.45-1-1v-2H9c-.55 0-1-.45-1-1s.45-1 1-1h2v-2c0-.55.45-1 1-1s1 .45 1 1v2h2c.55 0 1 .45 1 1s-.45 1-1 1zm-2-8V3.5L18.5 9H14c-.55 0-1-.45-1-1z" />
    </svg>
);

// Thay đổi 2: Xóa định nghĩa ImageInputIcon cũ

const CloseIcon = () => <svg viewBox="0 0 12 12"><path d="M6.707 6l4.146-4.146a.5.5 0 1 0-.707-.707L6 5.293 1.854 1.146a.5.5 0 0 0-.707.707L5.293 6l-4.147 4.146a.5.5 0 1 0 .707.707L6 6.707l4.146 4.146a.5.5 0 0 0 .707-.707z"></path></svg>;
const UploadIcon = () => <svg viewBox="0 0 24 24"><path d="M3.5 8.5c0 .55.45 1 1 1s1-.45 1-1v-2h2c.55 0 1-.45 1-1s-.45-1-1-1h-2v-2c0-.55-.45-1-1-1s-1 .45-1 1v2h-2c-.55 0-1 .45-1 1s.45 1 1 1h2v2zm10 3a3 3 0 1 0 0 6 3 3 0 0 0 0-6zm8-5h-3.17l-1.24-1.35a1.99 1.99 0 0 0-1.47-.65h-6.4c.17.3.28.63.28 1 0 1.1-.9 2-2 2h-1v1c0 1.1-.9 2-2 2-.37 0-.7-.11-1-.28V20.5c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2v-12c0-1.1-.9-2-2-2zm-8 13c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z"></path></svg>;

const UploadPanel = ({ onFileSelect, onClose }) => {
    const [isDragging, setIsDragging] = useState(false);

    const handleDragOver = (event) => { event.preventDefault(); setIsDragging(true); };
    const handleDragLeave = (event) => { event.preventDefault(); setIsDragging(false); };
    const handleDrop = (event) => {
        event.preventDefault();
        setIsDragging(false);
        const file = event.dataTransfer.files[0];
        if (file) {
            onFileSelect(file);
        }
    };
    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (file) {
            onFileSelect(file);
        }
    }

    return (
        <div className="upload-panel-container">
            <div className="upload-panel-header">
                <div className="input-types">
                    {/* Thay đổi 3: Sử dụng FileInputIcon mới ở đây */}
                    <button className="input-type-btn active"><FileInputIcon /></button>
                </div>
                <button className="close-panel-btn" onClick={onClose}><CloseIcon/></button>
            </div>
            <div className="upload-panel-body">
                <div className="upload-panel-left">
                    <h2>File Input</h2> {/* Cập nhật tiêu đề cho phù hợp */}
                    <div
                        className={`panel-drop-zone ${isDragging ? 'dragging' : ''}`}
                        onDragOver={handleDragOver}
                        onDragLeave={handleDragLeave}
                        onDrop={handleDrop}
                        onClick={() => document.getElementById('panelFileInput').click()}
                    >
                        <input
                            type="file"
                            id="panelFileInput"
                            hidden
                            accept=".pdf,.png,.jpeg,.jpg"
                            onChange={handleFileChange}
                        />
                        <FileInputIcon />
                        <span>Click, Drag, or Tap</span>
                        <small>to upload a file from your computer</small>
                    </div>
                    <div className="separator">OR</div>
                    <div className="url-input-group">
                        <label htmlFor="urlInput">Enter a URL for an image:</label>
                        <div className="url-input-wrapper">
                            <input id="urlInput" type="text" placeholder="https://..." />
                            <button>Go</button>
                        </div>
                    </div>
                </div>
                <div className="upload-panel-right">
                    <h3>Sample Documents</h3>
                    <p>Try these sample documents to see what the app can do.</p>
                    <div className="sample-images-grid">
                        <div className="sample-image"></div>
                        <div className="sample-image"></div>
                        <div className="sample-image"></div>
                        <div className="sample-image"></div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UploadPanel;
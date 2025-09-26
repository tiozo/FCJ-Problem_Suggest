// frontend/src/main.jsx

import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';

// Thay vì import './index.css', chúng ta import './App.css'
// để nạp các style toàn cục và biến màu cho theme.
import './styles/App.css';

// Đoạn mã còn lại hoàn toàn chính xác và hiện đại
ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>,
);
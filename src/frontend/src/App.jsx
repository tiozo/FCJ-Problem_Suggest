// frontend/src/App.jsx
import React, { useState, useEffect } from 'react';
import Header from './components/main_ui/Header';
import MainContent from './components/main_ui/mainContent';
import Footer from './components/main_ui/Footer';
import './styles/App.css';

function App() {
    // 1. Quản lý trạng thái theme, mặc định là 'light'
    // Chúng ta sẽ cố gắng đọc theme đã lưu từ localStorage
    const [theme, setTheme] = useState(() => {
        const savedTheme = localStorage.getItem('theme');
        return savedTheme ? savedTheme : 'light';
    });

    // 2. Hàm để chuyển đổi theme
    const toggleTheme = () => {
        const newTheme = theme === 'light' ? 'dark' : 'light';
        setTheme(newTheme);
    };

    // 3. Sử dụng useEffect để cập nhật class trên body và lưu vào localStorage
    // mỗi khi theme thay đổi
    useEffect(() => {
        // Xóa class cũ, thêm class mới
        document.body.classList.remove('light', 'dark');
        document.body.classList.add(theme);
        // Lưu lựa chọn theme của người dùng
        localStorage.setItem('theme', theme);
    }, [theme]);


    return (
        // 4. Áp dụng class theme vào container chính
        <div className="app-container">
            <Header theme={theme} onToggleTheme={toggleTheme} />
            <MainContent />
            <Footer />
        </div>
    );
}

export default App;
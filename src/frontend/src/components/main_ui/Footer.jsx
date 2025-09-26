// frontend/src/components/Footer.jsx
import React from 'react';
import '../styles/Footer.css';

const Footer = () => {
    return (
        <footer className="app-footer">
            <p>&copy; {new Date().getFullYear()} PDF Tagger Project. All rights reserved.</p>
        </footer>
    );
};

export default Footer;
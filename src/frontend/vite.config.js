import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Bất kỳ request nào bắt đầu bằng '/api' sẽ được chuyển đến target
      '/api': {
        target: 'http://localhost:8080', // URL của backend Spring Boot
        changeOrigin: true, // Cần thiết cho virtual hosted sites
        //rewrite: (path) => path.replace(/^\/api/, ''), // Bỏ '/api' trước khi gửi đi
      },
    },
  },
})

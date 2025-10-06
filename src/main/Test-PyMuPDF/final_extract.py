import fitz
import PyPDF2

# Extract embedded images using PyPDF2
reader = PyPDF2.PdfReader("de-thi-thu-tn-thpt-2025-mon-toan-lan-2-truong-thpt-huong-hoa-quang-tri.pdf")
image_count = 0

for page_num, page in enumerate(reader.pages):
    for image_obj in page.images:
        # Save as PNG for consistency
        with open(f"figure_{image_count}.png", "wb") as fp:
            fp.write(image_obj.data)
            print(f"Extracted embedded image: figure_{image_count}")
            image_count += 1

# Extract using PyMuPDF
doc = fitz.open("de-thi-thu-tn-thpt-2025-mon-toan-lan-2-truong-thpt-huong-hoa-quang-tri.pdf")
figure_count = image_count

for page_num in range(doc.page_count):
    page = doc[page_num]
    
    # Extract tables
    tables = page.find_tables()
    for i, table in enumerate(tables):
        bbox = table.bbox
        table_rect = fitz.Rect(bbox[0]-2, bbox[1]-2, bbox[2]+2, bbox[3]+2)
        pix = page.get_pixmap(matrix=fitz.Matrix(2, 2), clip=table_rect)
        pix.save(f"table_page{page_num+1}_{i}.png")
        print(f"Extracted table: table_page{page_num+1}_{i}.png")
    
    # Extract figure regions (like the Cau 6 geometric figure)
    drawings = page.get_drawings()
    if drawings:
        rects = []
        for drawing in drawings:
            rect = drawing["rect"]
            if rect.width > 15 and rect.height > 15:
                rects.append(rect)
        
        if rects:
            # Merge all nearby rectangles into one figure
            merged_rect = rects[0]
            for rect in rects[1:]:
                expanded = merged_rect + (-30, -30, 30, 30)
                if rect.intersects(expanded):
                    merged_rect = merged_rect | rect
            
            # Extract if it's a significant figure
            if merged_rect.width > 50 and merged_rect.height > 40:
                region = merged_rect + (-15, -15, 15, 15)
                pix = page.get_pixmap(matrix=fitz.Matrix(2, 2), clip=region)
                pix.save(f"figure_{figure_count}.png")
                print(f"Extracted figure: figure_{figure_count}.png")
                figure_count += 1

doc.close()
print(f"Total figures/tables extracted: {figure_count}")
import sys
import os
import fitz
import PyPDF2

def extract_figures(pdf_path):
    """Extract all figures and tables from PDF and save as <PDF_name>/<figure_order>.png"""
    
    # Get PDF filename without extension
    pdf_name = os.path.splitext(os.path.basename(pdf_path))[0]
    
    # Create output directory
    output_dir = pdf_name
    os.makedirs(output_dir, exist_ok=True)
    
    figure_order = 1
    
    # Extract embedded images using PyPDF2
    reader = PyPDF2.PdfReader(pdf_path)
    for page_num, page in enumerate(reader.pages):
        for image_obj in page.images:
            output_path = os.path.join(output_dir, f"{figure_order}.png")
            with open(output_path, "wb") as fp:
                fp.write(image_obj.data)
            print(f"Extracted: {output_path}")
            figure_order += 1
    
    # Extract figures and tables using PyMuPDF
    doc = fitz.open(pdf_path)
    
    for page_num in range(doc.page_count):
        page = doc[page_num]
        
        # Extract figure regions
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
                    output_path = os.path.join(output_dir, f"{figure_order}.png")
                    pix.save(output_path)
                    print(f"Extracted: {output_path}")
                    figure_order += 1
    
    doc.close()
    return figure_order - 1

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python extract_pdf_figures.py <pdf_path>")
        sys.exit(1)
    
    pdf_path = sys.argv[1]
    if not os.path.exists(pdf_path):
        print(f"Error: File {pdf_path} not found")
        sys.exit(1)
    
    try:
        total_figures = extract_figures(pdf_path)
        print(f"SUCCESS: Extracted {total_figures} figures")
    except Exception as e:
        print(f"ERROR: {str(e)}")
        sys.exit(1)
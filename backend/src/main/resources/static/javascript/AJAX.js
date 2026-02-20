// ajax.js
let currentPage = 1; 

document.addEventListener('DOMContentLoaded', () => {
    const loadMoreBtn = document.getElementById('load-more');
    const spinner = document.getElementById('spinner');
    const container = document.getElementById('product-container');

    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', function() {
            // Feedback visual
            spinner.classList.remove('d-none');
            this.disabled = true;

            fetch(`/load-more-products?page=${currentPage}`)
                .then(response => {
                    if (!response.ok) throw new Error('Error al cargar productos');
                    return response.text();
                })
                .then(html => {
                    // Si el servidor devuelve el fragmento vacío, ya no hay más productos
                    if (html.trim() === "") {
                        this.innerText = "No more treasures found";
                        this.classList.replace('btn-load-more', 'btn-secondary');
                        this.disabled = true;
                    } else {
                        // Insertamos el HTML de las nuevas cards
                        container.insertAdjacentHTML('beforeend', html);
                        currentPage++;
                        this.disabled = false;
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert("No se pudieron cargar más productos.");
                    this.disabled = false;
                })
                .finally(() => {
                    spinner.classList.add('d-none');
                });
        });
    }
});
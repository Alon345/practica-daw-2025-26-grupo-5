// ajax.js
let currentPage = 1; 

document.addEventListener('DOMContentLoaded', () => {
    const loadMoreBtn = document.getElementById('load-more');
    const spinner = document.getElementById('spinner');
    const container = document.getElementById('product-container');

    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', function() {
            // Visual Feedback
            spinner.classList.remove('d-none');
            this.disabled = true;

            fetch(`/load-more-products?page=${currentPage}`)
                .then(response => {
                    if (!response.ok) throw new Error('Error al cargar productos');
                    return response.text();
                })
                .then(html => {
                    // If the server returns an empty fragment, there are no more products
                    if (html.trim() === "") {
                        this.innerText = "No more treasures found";
                        this.classList.replace('btn-load-more', 'btn-no-more');
                        this.disabled = true;
                    } else {
                        // We insert the HTML of the new cards
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
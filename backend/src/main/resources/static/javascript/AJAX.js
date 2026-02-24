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
                    container.insertAdjacentHTML('beforeend', html);

                    const noMoreMarker = container.querySelector('#no-more-marker');

                    if (noMoreMarker) {
                        this.innerText = "No more treasures found";
                        this.classList.replace('btn-load-more', 'btn-no-more');
                        this.disabled = true;

                        noMoreMarker.remove();
                    } else {
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
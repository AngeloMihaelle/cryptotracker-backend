$(document).ready(function() {
    // Variables globales
    let cryptoData = [];
    let currentPage = 'home';
    let priceUpdateInterval;

    // Mapeo de símbolos a IDs de CoinGecko
    const symbolToId = {
        'btc': 'bitcoin',
        'eth': 'ethereum',
        'usdt': 'tether',
        'bnb': 'binancecoin',
        'xrp': 'ripple',
        'ada': 'cardano',
        'doge': 'dogecoin',
        'matic': 'polygon',
        'sol': 'solana',
        'dot': 'polkadot'
    };

    // Colores para las criptomonedas
    const cryptoColors = {
        'bitcoin': '#f7931a',
        'ethereum': '#627eea',
        'tether': '#26a17b',
        'binancecoin': '#f0b90b',
        'ripple': '#000000',
        'cardano': '#0033ad',
        'dogecoin': '#c3a634',
        'polygon': '#8247e5',
        'solana': '#00ffa3',
        'polkadot': '#e6007a'
    };

    // Navegación
    $('.nav-link, .feature-card').on('click', function(e) {
        e.preventDefault();
        const targetPage = $(this).data('page');
        if (targetPage) {
            switchPage(targetPage);
        }
    });

    // Menu móvil
    $('#menuToggle').on('click', function() {
        $('#navMenu').toggleClass('active');
    });

    // Función para cambiar de página
    function switchPage(pageName) {
        currentPage = pageName;
        $('.page').removeClass('active');
        $(`#${pageName}`).addClass('active');
        $('.nav-link').removeClass('active');
        $(`.nav-link[data-page="${pageName}"]`).addClass('active');
        
        // Cargar datos específicos de la página
        switch(pageName) {
            case 'current-prices':
                loadCurrentPrices();
                startPriceUpdates();
                break;
            case 'price-history':
                loadCryptoOptions('#cryptoSelect');
                break;
            case 'multi-charts':
                break;
            case 'comparison':
                loadCryptoCheckboxes();
                break;
            case 'regression':
                loadCryptoOptions('#regressionSelect');
                break;
            default:
                stopPriceUpdates();
        }
    }

    // Cargar precios actuales
    function loadCurrentPrices() {
        $.ajax({
            url: '/api/prices/latest',
            method: 'GET',
            success: function(data) {
                cryptoData = data;
                displayCurrentPrices(data);
            },
            error: function() {
                $('#cryptoList').html('<p class="error">Error al cargar los datos</p>');
            }
        });
    }

    // Función para obtener URL del logo de criptomoneda
    function getCryptoLogoUrl(crypto) {
        // Usar la imagen del objeto si existe, si no usar CryptoCompare como fallback
        if (crypto.image) {
            return crypto.image;
        }
        // Fallback a CryptoCompare
        return `https://www.cryptocompare.com/media/37746251/${crypto.symbol.toLowerCase()}.png`;
    }

    // Mostrar precios actuales con logos
    function displayCurrentPrices(data) {
        let html = '';
        data.forEach(crypto => {
            const changeClass = crypto.price_change_percentage_24h >= 0 ? 'positive' : 'negative';
            const changeSymbol = crypto.price_change_percentage_24h >= 0 ? '+' : '';
            const iconColor = cryptoColors[crypto.id] || '#888';
            const logoUrl = getCryptoLogoUrl(crypto);
            
            html += `
                <div class="crypto-item">
                    <div class="crypto-info">
                        <div class="crypto-icon" style="background: ${iconColor}20;">
                            <img src="${logoUrl}" alt="${crypto.name}" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                            <div class="crypto-icon-fallback" style="display:none; color: ${iconColor};">
                                ${crypto.symbol.toUpperCase()}
                            </div>
                        </div>
                        <div class="crypto-details">
                            <h3>${crypto.name}</h3>
                            <p>${crypto.symbol.toUpperCase()}</p>
                        </div>
                    </div>
                    <div class="crypto-price">
                        <div class="price-value">$${crypto.current_price.toLocaleString()}</div>
                        <div class="price-change ${changeClass}">
                            ${changeSymbol}${crypto.price_change_percentage_24h.toFixed(2)}%
                        </div>
                    </div>
                </div>
            `;
        });
        $('#cryptoList').html(html);
    }

    // Iniciar actualización automática de precios
    function startPriceUpdates() {
        stopPriceUpdates();
        priceUpdateInterval = setInterval(loadCurrentPrices, 60000); // Cada minuto
    }

    // Detener actualización automática
    function stopPriceUpdates() {
        if (priceUpdateInterval) {
            clearInterval(priceUpdateInterval);
        }
    }

    // Cargar opciones de criptomonedas en select
    function loadCryptoOptions(selector) {
        $.ajax({
            url: '/api/prices/latest',
            method: 'GET',
            success: function(data) {
                let options = '<option value="">Selecciona una criptomoneda</option>';
                data.forEach(crypto => {
                    options += `<option value="${crypto.id}">${crypto.name} (${crypto.symbol.toUpperCase()})</option>`;
                });
                $(selector).html(options);
            }
        });
    }

    // Cargar checkboxes de criptomonedas con logos
    function loadCryptoCheckboxes() {
        $.ajax({
            url: '/api/prices/latest',
            method: 'GET',
            success: function(data) {
                let html = '';
                data.forEach(crypto => {
                    const iconColor = cryptoColors[crypto.id] || '#888';
                    const logoUrl = getCryptoLogoUrl(crypto);
                    html += `
                        <label class="checkbox-item">
                            <input type="checkbox" value="${crypto.id}" name="crypto">
                            <img src="${logoUrl}" width="20" height="20" style="border-radius: 50%;" 
                                 onerror="this.style.display='none'; this.nextElementSibling.style.display='inline';">
                            <span style="color: ${iconColor}; display:none;">${crypto.symbol.toUpperCase()}</span>
                            <span>${crypto.name}</span>
                        </label>
                    `;
                });
                $('#cryptoCheckboxes').html(html);
            }
        });
    }

    // Range sliders
    $('input[type="range"]').on('input', function() {
        const valueSpan = $(this).siblings('.range-value');
        valueSpan.text($(this).val());
    });

    // Inicializar valores de los sliders
    $('#compTimeValue').text($('#compTimeRange').val());
    $('#regressionTimeValue').text($('#regressionTimeRange').val());

    // Página 2: Historial individual - Ahora con imagen real
    $('#showHistory').on('click', function() {
        const cryptoId = $('#cryptoSelect').val();
        const hours = $('#timeRange').val();
        
        if (!cryptoId) {
            alert('Por favor selecciona una criptomoneda');
            return;
        }

        $('#historyChart').html('<div class="loading"><div class="spinner"></div><p>Generando gráfica...</p></div>');
        
        // Usar el nuevo endpoint de imagen
        const imageUrl = `/api/chart/single/${cryptoId}?hours=${hours}`;
        
        // Crear imagen y manejar carga
        const img = new Image();
        img.onload = function() {
            $('#historyChart').html(`<img src="${imageUrl}" alt="Gráfica de ${cryptoId}">`);
        };
        img.onerror = function() {
            $('#historyChart').html('<p class="error">Error al cargar la gráfica</p>');
        };
        img.src = imageUrl;
    });

    // Página 3: Gráficas múltiples - Ahora con imágenes reales
    $('#showMultiCharts').on('click', function() {
        const hours = $('#multiTimeRange').val();
        
        $('#multiChartsGrid').html('<div class="loading"><div class="spinner"></div><p>Generando gráficas...</p></div>');
        
        // Obtener lista de top 10
        $.ajax({
            url: '/api/prices/latest',
            method: 'GET',
            success: function(cryptos) {
                let html = '';
                
                cryptos.forEach((crypto, index) => {
                    const iconColor = cryptoColors[crypto.id] || '#888';
                    const logoUrl = getCryptoLogoUrl(crypto);
                    const chartUrl = `/api/chart/single/${crypto.id}?hours=${hours}`;
                    
                    html += `
                        <div class="chart-item">
                            <div class="chart-header">
                                <div class="crypto-icon" style="background: ${iconColor}20;">
                                    <img src="${logoUrl}" alt="${crypto.name}">
                                </div>
                                <h3>${crypto.name}</h3>
                            </div>
                            <div class="chart-placeholder" id="chart-${crypto.id}">
                                <img src="${chartUrl}" alt="Gráfica de ${crypto.name}" 
                                     onload="this.style.display='block';" 
                                     onerror="this.parentElement.innerHTML='<p style=\"color: #ff3a3a;\">Error al cargar gráfica</p>';">
                            </div>
                        </div>
                    `;
                });
                
                $('#multiChartsGrid').html(html);
            }
        });
    });

    // Función auxiliar para convertir horas a formato HH:mm
    function calculateTimeRange(hours) {
        const now = new Date();
        const startTime = new Date(now.getTime() - (hours * 60 * 60 * 1000));
        
        const startHour = String(startTime.getHours()).padStart(2, '0');
        const startMin = String(startTime.getMinutes()).padStart(2, '0');
        const endHour = String(now.getHours()).padStart(2, '0');
        const endMin = String(now.getMinutes()).padStart(2, '0');
        
        return {
            start: `${startHour}:${startMin}`,
            end: `${endHour}:${endMin}`
        };
    }

    // Página 4: Comparación - Usando endpoint overlay
    $('#showComparison').on('click', function() {
        const selectedCryptos = $('input[name="crypto"]:checked').map(function() {
            return $(this).val();
        }).get();
        
        const hours = $('#compTimeRange').val();
        
        if (selectedCryptos.length < 2) {
            alert('Por favor selecciona al menos 2 criptomonedas');
            return;
        }

        $('#comparisonChart').html('<div class="loading"><div class="spinner"></div><p>Generando comparación...</p></div>');
        
        // Convertir horas a formato HH:mm
        const timeRange = calculateTimeRange(hours);
        
        // Construir URL con el nuevo endpoint overlay
        const symbolsParam = selectedCryptos.join(',');
        const overlayUrl = `/api/chart/overlay?symbols=${symbolsParam}&start=${timeRange.start}&end=${timeRange.end}`;
        
        // Crear imagen
        const img = new Image();
        img.onload = function() {
            $('#comparisonChart').html(`<img src="${overlayUrl}" alt="Comparación de criptomonedas">`);
        };
        img.onerror = function() {
            $('#comparisonChart').html('<p class="error">Error al generar la comparación</p>');
        };
        img.src = overlayUrl;
    });

    // Página 5: Regresión lineal - Usando endpoint regression
    $('#showRegression').on('click', function() {
        const cryptoId = $('#regressionSelect').val();
        const hours = $('#regressionTimeRange').val();
        
        if (!cryptoId) {
            alert('Por favor selecciona una criptomoneda');
            return;
        }

        $('#regressionChart').html('<div class="loading"><div class="spinner"></div><p>Calculando regresión...</p></div>');
        
        // Convertir horas a formato HH:mm
        const timeRange = calculateTimeRange(hours);
        
        // Usar endpoint de regresión
        const regressionUrl = `/api/chart/regression/${cryptoId}?start=${timeRange.start}&end=${timeRange.end}`;
        
        // Crear imagen
        const img = new Image();
        img.onload = function() {
            $('#regressionChart').html(`<img src="${regressionUrl}" alt="Análisis de regresión de ${cryptoId}">`);
        };
        img.onerror = function() {
            $('#regressionChart').html('<p class="error">Error al calcular la regresión</p>');
        };
        img.src = regressionUrl;
    });

    // Inicializar con la página home
    switchPage('home');
});
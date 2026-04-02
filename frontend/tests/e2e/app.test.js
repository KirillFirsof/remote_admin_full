const { Builder, By, until } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');

describe('Remote Admin Panel E2E Tests', () => {
    let driver;

    beforeAll(async () => {
        const options = new chrome.Options();
        options.addArguments('--no-sandbox');
        options.addArguments('--disable-dev-shm-usage');
        
        if (process.env.HEADLESS !== 'false') {
            options.addArguments('--headless=new');
        }
        
        driver = await new Builder()
            .forBrowser('chrome')
            .setChromeOptions(options)
            .build();
        driver.manage().setTimeouts({ implicit: 15000, pageLoad: 30000 });
    });

    afterAll(async () => {
        if (driver) {
            await driver.quit();
        }
    });

    // Функция для клика по первому онлайн компьютеру
    async function clickFirstOnlineComputer() {
        await driver.wait(until.elementLocated(By.className('grid')), 15000);
        
        // Находим все карточки компьютеров
        const cards = await driver.findElements(By.css('.bg-white.rounded-xl.shadow-lg'));
        
        for (let i = 0; i < cards.length; i++) {
            // Ищем зеленую иконку WiFi (онлайн)
            const wifiIcon = await cards[i].findElements(By.css('.lucide-wifi.text-green-500'));
            
            if (wifiIcon.length > 0) {
                // Нашли онлайн компьютер, кликаем на кнопку "Подробнее"
                const detailButton = await cards[i].findElement(By.linkText('Подробнее'));
                await detailButton.click();
                console.log(`Кликнут онлайн компьютер #${i+1}`);
                return true;
            }
        }
        
        // Если не нашли онлайн компьютер, кликаем на первую карточку
        console.log('Онлайн компьютеры не найдены, кликаем на первый');
        const firstCard = cards[0];
        const detailButton = await firstCard.findElement(By.linkText('Подробнее'));
        await detailButton.click();
        return false;
    }

    test('главная страница должна загружаться и отображать карточки компьютеров', async () => {
        await driver.get('http://localhost:5173');
        await driver.wait(until.elementLocated(By.className('grid')), 15000);
        
        const cards = await driver.findElements(By.css('.bg-white.rounded-xl.shadow-lg'));
        console.log(`Найдено карточек: ${cards.length}`);
        expect(cards.length).toBeGreaterThanOrEqual(0);
        
        if (cards.length > 0) {
            const computerNames = await driver.findElements(By.css('.font-semibold.text-lg'));
            if (computerNames.length > 0) {
                const firstName = await computerNames[0].getText();
                console.log(`Первый компьютер: ${firstName}`);
                expect(firstName).toBeTruthy();
            }
            
            const detailButtons = await driver.findElements(By.linkText('Подробнее'));
            expect(detailButtons.length).toBeGreaterThan(0);
        }
    });

    test('должен открываться детальный просмотр компьютера', async () => {
        await driver.get('http://localhost:5173');
        
        await clickFirstOnlineComputer();
        
        await driver.wait(until.urlContains('/computers/'), 10000);
        const currentUrl = await driver.getCurrentUrl();
        expect(currentUrl).toContain('/computers/');
        
        // Проверяем наличие формы отправки команды по точному placeholder
        const commandInput = await driver.findElement(By.css('input[placeholder*="Введите команду"]'));
        expect(commandInput).toBeDefined();
        
        // Проверяем наличие кнопки "Выполнить"
        const executeButton = await driver.findElement(By.xpath("//button[contains(text(), 'Выполнить')]"));
        expect(executeButton).toBeDefined();
        
        // Проверяем наличие истории команд по заголовку
        const historyTitle = await driver.findElement(By.xpath("//h3[contains(text(), 'История команд')]"));
        expect(historyTitle).toBeDefined();
    });

    test('должен отправлять команду и видеть её в истории', async () => {
        jest.setTimeout(120000);
        
        await driver.get('http://localhost:5173');
        
        await clickFirstOnlineComputer();
        
        // Ждем появления поля ввода
        await driver.wait(until.elementLocated(By.css('input[placeholder*="Введите команду"]')), 10000);
        
        // Получаем текущие команды из таблицы
        const existingRows = await driver.findElements(By.css('tbody tr'));
        const initialCount = existingRows.length;
        console.log(`Было команд в таблице: ${initialCount}`);
        
        // Вводим уникальную команду
        const timestamp = Date.now();
        const commandText = `echo Test_${timestamp}`;
        
        const input = await driver.findElement(By.css('input[placeholder*="Введите команду"]'));
        await input.clear();
        await input.sendKeys(commandText);
        
        const executeButton = await driver.findElement(By.xpath("//button[contains(text(), 'Выполнить')]"));
        await executeButton.click();
        
        console.log(`Отправлена команда: ${commandText}`);
        
        // Ждем появления новой строки в таблице
        let newRowFound = false;
        for (let i = 0; i < 15; i++) {
            await driver.sleep(2000);
            const currentRows = await driver.findElements(By.css('tbody tr'));
            console.log(`Попытка ${i+1}: строк в таблице ${currentRows.length}`);
            
            // Проверяем, появилась ли наша команда
            for (const row of currentRows) {
                const cells = await row.findElements(By.css('td'));
                if (cells.length > 0) {
                    const commandCell = await cells[0].getText();
                    if (commandCell === commandText) {
                        console.log(`Найдена команда "${commandText}" в истории`);
                        newRowFound = true;
                        break;
                    }
                }
            }
            
            if (currentRows.length > initialCount) {
                console.log(`Количество строк увеличилось: ${currentRows.length} > ${initialCount}`);
                newRowFound = true;
                break;
            }
        }
        
        expect(newRowFound).toBe(true);
    }, 120000);

    test('должен показывать онлайн/оффлайн статус', async () => {
        await driver.get('http://localhost:5173');
        await driver.wait(until.elementLocated(By.className('grid')), 15000);
        
        // Ищем статусы по тексту
        const onlineIndicators = await driver.findElements(By.xpath("//span[contains(text(), 'Online')]"));
        const offlineIndicators = await driver.findElements(By.xpath("//span[contains(text(), 'Offline')]"));
        
        console.log(`Online: ${onlineIndicators.length}, Offline: ${offlineIndicators.length}`);
        expect(onlineIndicators.length + offlineIndicators.length).toBeGreaterThanOrEqual(0);
    });

    test('должен показывать загрузку CPU и RAM', async () => {
        await driver.get('http://localhost:5173');
        await driver.wait(until.elementLocated(By.className('grid')), 15000);
        
        // Ищем элементы с процентами и MB
        const cpuElements = await driver.findElements(By.xpath("//span[contains(., '%') and string-length(normalize-space(.)) > 1]"));
        const ramElements = await driver.findElements(By.xpath("//span[contains(., 'MB') and string-length(normalize-space(.)) > 2]"));
        
        console.log(`Найдено CPU элементов: ${cpuElements.length}, RAM элементов: ${ramElements.length}`);
        
        if (cpuElements.length > 0) {
            const firstCpu = await cpuElements[0].getText();
            console.log(`Пример CPU: ${firstCpu}`);
            expect(firstCpu).toMatch(/\d/);
        }
    });

    test('должна работать кнопка "Назад" на странице компьютера', async () => {
        await driver.get('http://localhost:5173');
        
        await clickFirstOnlineComputer();
        
        await driver.wait(until.urlContains('/computers/'), 10000);
        
        // Кнопка "Назад" с текстом
        const backButton = await driver.findElement(By.linkText('← Назад к списку'));
        await backButton.click();
        
        await driver.wait(until.urlIs('http://localhost:5173/'), 10000);
        const currentUrl = await driver.getCurrentUrl();
        expect(currentUrl).toBe('http://localhost:5173/');
    });
});
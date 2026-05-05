import fs from 'fs';

const arabicXml = fs.readFileSync('quran-uthmani-min.xml', 'utf-8');
const englishXml = fs.readFileSync('en.sahih.xml', 'utf-8');

function parseXml(xmlString) {
    const suras = [];
    let currentSura = null;
    
    const lines = xmlString.split('\n');
    for (const line of lines) {
        const suraMatch = line.match(/<sura index="(\d+)" name="([^"]+)">/);
        if (suraMatch) {
            currentSura = {
                index: parseInt(suraMatch[1]),
                name: suraMatch[2],
                ayas: {}
            };
            suras.push(currentSura);
            continue;
        }
        
        const ayaMatch = line.match(/<aya index="(\d+)" text="([^"]+)"/);
        if (ayaMatch && currentSura) {
            currentSura.ayas[parseInt(ayaMatch[1])] = ayaMatch[2];
        }
    }
    
    return suras;
}

const arabicSuras = parseXml(arabicXml);
const englishSuras = parseXml(englishXml);

const englishMap = new Map();
englishSuras.forEach(s => englishMap.set(s.index, s));

const quranGroups = [];
const MAX_LENGTH = 120; // light and short

for (const arSura of arabicSuras) {
    const enSura = englishMap.get(arSura.index) || { ayas: {}, name: '' };
    // Prefer English name if it exists because it's romanized usually, but here we can just use the Arabic name or English name.
    // The original logic did: if (!engSuraName.isNullOrBlank()) engSuraName else suraData.first
    const suraName = enSura.name || arSura.name;
    
    const ayasAr = arSura.ayas;
    const ayasEn = enSura.ayas;
    
    const maxAya = Math.max(...Object.keys(ayasAr).map(Number));
    
    let currentGroupAr = [];
    let currentGroupEn = [];
    let startAya = -1;
    let endAya = -1;
    let currentLen = 0;
    
    function saveGroup() {
        if (currentGroupAr.length > 0) {
            const arText = currentGroupAr.join(' ۞ ');
            const enText = currentGroupEn.join(' ');
            
            // Only save if it's not too long and not empty
            if (arText.length <= 150) {
                const displaySuraName = suraName || `Sura ${arSura.index}`;
                const sourceText = startAya === endAya ? `${displaySuraName} - Ayah ${startAya}` : `${displaySuraName} - Ayahs ${startAya}-${endAya}`;
                quranGroups.push({
                    arabicText: arText,
                    englishText: enText,
                    source: sourceText,
                    explanation: ""
                });
            }
            
            currentGroupAr = [];
            currentGroupEn = [];
            startAya = -1;
            endAya = -1;
            currentLen = 0;
        }
    }
    
    for (let ayaIndex = 1; ayaIndex <= maxAya; ayaIndex++) {
        const arText = ayasAr[ayaIndex];
        if (!arText) continue;
        const enText = ayasEn[ayaIndex] || "";
        
        // Skip ayahs that are inherently too long
        if (arText.length > MAX_LENGTH) {
            saveGroup(); // flush current
            continue;
        }
        
        if (currentLen + arText.length + 3 > MAX_LENGTH) {
            saveGroup();
        }
        
        if (currentGroupAr.length === 0) {
            startAya = ayaIndex;
            endAya = ayaIndex;
            currentGroupAr.push(arText);
            currentGroupEn.push(enText);
            currentLen = arText.length;
        } else {
            endAya = ayaIndex;
            currentGroupAr.push(arText);
            currentGroupEn.push(enText);
            currentLen += arText.length + 3;
        }
    }
    saveGroup();
}

const outputPath = 'android-wird-app/app/src/main/assets/quran_wirds.json';
fs.writeFileSync(outputPath, JSON.stringify(quranGroups, null, 2));

console.log(`Successfully generated ${outputPath}`);
console.log(`Total short Wird items extracted: ${quranGroups.length}`);

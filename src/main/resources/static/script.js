document.getElementById('convertNumberForm').onsubmit = async function (event) {
    event.preventDefault();
    
    const number = document.getElementById('numberInput').value;
    const textResult = document.getElementById('textResult');
    const audioPlayer = document.getElementById('audioPlayer');
    const downloadLink = document.getElementById('downloadLink');
    
    textResult.innerText = '';
    audioPlayer.style.display = 'none';
    downloadLink.style.display = 'none';
    
    if (!number) {
        textResult.innerText = 'Error: Please enter a valid number.';
        textResult.style.color = 'red';
        return;
    }
    
    try {
        const response = await fetch(`/convert-to-words-and-audio?number=${encodeURIComponent(number)}`);
        const data = await response.json();
        
        if (response.ok) {
            textResult.innerText = data.words;
            textResult.style.color = 'black';
            
            // Set up the audio player
            const audioUrl = data.audioFileUrl;
            const host = window.location.host;
            audioPlayer.src = `http://${host}/audio/${audioUrl.split('/').pop()}`;
            audioPlayer.style.display = 'block';
            
            // Set up the download link
            downloadLink.href = audioUrl;
            downloadLink.download = audioUrl.split('/').pop() ;
            downloadLink.innerText = 'Download Audio';
            downloadLink.style.display = 'block';
        } else {
            textResult.innerText = data.error;
            textResult.style.color = 'red';
        }
    } catch (error) {
        console.error(error);
        textResult.innerText = 'Error: Unable to process your request.';
        textResult.style.color = 'red';
    }
};

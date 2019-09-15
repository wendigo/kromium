# How to work with this thing ;-)

### Install and run chrome-protocol-proxy (debugging proxy)

```go get -u github.com/wendigo/chrome-protocol-proxy```
```chrome-protocol-proxy -m -delta -i```

### Run headless_shell in Docker (most stable option right now)
```docker run  -p 9222:9222 --net="bridge" yukinying/chrome-headless:latest```

### Or run Chromium
```
/Applications/Chromium.app/Contents/MacOS/Chromium --remote-debugging-port=9222 --enable-experimental-extension-apis --no-default-browser-check --headless --disable-gpu --hide-scrollbars
```

### Run tests
```
LOOPBACK_ADDRESS=192.168.1.179 ./gradlew clean test
```
### Enjoy :)

### Order and meaning of debugger events

- Page.frameNavigated - frame was navigated to new url
- Page.frameStartedLoading - frame started loading
- Page.frameStoppedLoading - frame was loaded and DOM parsing started
- DOM.documentUpdated - all document ids are now invalid (invalidated)
- Page.domContentEventFired - dom content ready?
- Page.loadEventFired - window.onload


Events orders: Page.frameStartedLoading > Page.frameNavigated > Page.loadEventFired > Page.frameStoppedLoading > Page.domContentEventFired
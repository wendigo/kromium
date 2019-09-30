# Known bugs

## Closing headless session leads to invalid state
```
22:18:10.331 INFO  pl.wendigo.chrome.headless.TargetedFramesStream: Could not close target due to exception pl.wendigo.chrome.protocol.RequestFailed: request = RequestFrame(id=43, method=Target.closeTarget, params=CloseTargetRequest(targetId=22C7D491970D0EF644F0B0378C83AC8B)), error = Could not enqueue message
```

# Things to refactor

## Waiting for frame to load

- Use frameId from navigate() to provide condition to wait upon

# New features

- Download Manager
- Password Manager
- http://screensiz.es/ to provide devices database

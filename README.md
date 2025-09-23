# 2p2tcore

A plugin for anarchy servers

## Features

### Chunk Loading Management

To prevent server-side vulnerabilities and lag caused by players exploiting chunk loading, a new chunk loading management system has been implemented. This system limits the number of chunks a player can force load.

#### Configuration

The maximum number of chunks a player can force load can be configured in the `config.yml` file.

```yaml
MaxChunksPerPlayer: 10 # Default value is 10. Adjust as needed.
```

If a player attempts to force load more chunks than the configured limit, they will receive a message indicating that they have reached the maximum limit, and the chunk will not be loaded.

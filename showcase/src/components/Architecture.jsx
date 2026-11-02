import { useState } from 'react';
import { Box, ButtonBase, Chip, Paper, Stack, Typography } from '@mui/material';
import { alpha } from '@mui/material/styles';
import { modules } from '../data';
import { ember, glass, mono } from '../theme';

// Edges of the system, shown as small labels between the module nodes.
const links = { app: 'HTTPS · JSON · JWT', server: 'JDBC' };

function Node({ m, active, onSelect }) {
  return (
    <ButtonBase
      onClick={() => onSelect(m.id)}
      aria-pressed={active}
      sx={{
        ...glass,
        display: 'block', textAlign: 'left', width: '100%', borderRadius: 2, px: 2, py: 1.5,
        transition: 'border-color .2s, background .2s',
        borderColor: active ? alpha(ember, 0.6) : 'rgba(255,255,255,0.08)',
        background: active ? alpha(ember, 0.08) : glass.background,
        '&:hover': { borderColor: alpha(ember, 0.4) },
      }}
    >
      <Typography sx={{ fontFamily: mono, fontSize: '0.85rem', color: active ? 'primary.main' : 'text.primary' }}>{m.path}</Typography>
      <Typography variant="body2" color="text.secondary">{m.label}</Typography>
    </ButtonBase>
  );
}

function Edge({ label }) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1, py: 0.5, fontFamily: mono, fontSize: '0.68rem', color: 'text.secondary' }}>
      <Box sx={{ width: '1px', height: 18, bgcolor: 'rgba(255,255,255,0.2)' }} />
      {label}
    </Box>
  );
}

export default function Architecture() {
  const [selected, setSelected] = useState('server');
  const current = modules.find((m) => m.id === selected);
  const byId = Object.fromEntries(modules.map((m) => [m.id, m]));

  return (
    <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '260px 1fr' }, gap: 3 }}>
      {/* Dependency graph: app → server → db, with shared used by app and server. */}
      <Box>
        <Node m={byId.app} active={selected === 'app'} onSelect={setSelected} />
        <Edge label={links.app} />
        <Node m={byId.server} active={selected === 'server'} onSelect={setSelected} />
        <Edge label={links.server} />
        <Node m={byId.db} active={selected === 'db'} onSelect={setSelected} />
        <Box sx={{ mt: 2, pt: 2, borderTop: '1px dashed rgba(255,255,255,0.12)' }}>
          <Typography sx={{ fontFamily: mono, fontSize: '0.68rem', color: 'text.secondary', mb: 1 }}>compiled into app/ and server/</Typography>
          <Node m={byId.shared} active={selected === 'shared'} onSelect={setSelected} />
        </Box>
      </Box>

      <Paper key={current.id} sx={{ p: { xs: 2.5, sm: 3 }, animation: 'fadeIn .25s ease' }}>
        <Typography sx={{ fontFamily: mono, color: 'primary.main', fontSize: '0.8rem', mb: 0.5 }}>{current.path}</Typography>
        <Typography variant="h3" sx={{ mb: 2 }}>{current.label}</Typography>
        <Box component="ul" sx={{ m: 0, mb: 3, pl: 2.5, '& li': { mb: 0.75 }, '& li::marker': { color: ember } }}>
          {current.notes.map((n) => (
            <Typography component="li" key={n} variant="body1">{n}</Typography>
          ))}
        </Box>
        <Stack direction="row" sx={{ flexWrap: 'wrap', gap: 1 }}>
          {current.stack.map((s) => <Chip key={s} size="small" variant="outlined" label={s} />)}
        </Stack>
      </Paper>
    </Box>
  );
}

import { Box, Chip, Paper, Typography } from '@mui/material';
import { stack } from '../data';
import { mono } from '../theme';

export default function Stack() {
  return (
    <Paper sx={{ p: 0, overflow: 'hidden' }}>
      {stack.map((s, i) => (
        <Box
          key={s.layer}
          sx={{
            display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '170px 1fr' }, gap: { xs: 1, sm: 2 }, alignItems: 'start',
            px: { xs: 2, sm: 2.5 }, py: 1.5, borderTop: i ? '1px solid rgba(255,255,255,0.05)' : 'none',
            opacity: s.layer === 'Coursework tools' ? 0.75 : 1,
          }}
        >
          <Typography sx={{ fontFamily: mono, fontSize: '0.78rem', pt: 0.4 }}>{s.layer}</Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
            {s.items.map((t) => <Chip key={t} size="small" variant="outlined" label={t} />)}
          </Box>
        </Box>
      ))}
    </Paper>
  );
}

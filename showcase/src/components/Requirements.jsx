import { Box, Paper, Typography } from '@mui/material';
import { requirements } from '../data';
import { mono } from '../theme';

const groups = [...new Set(requirements.map((r) => r.group))];

// Requirements from the coursework proposal, with how the rewrite meets each one.
export default function Requirements() {
  return (
    <Paper sx={{ p: 0, overflow: 'hidden' }}>
      <Box
        sx={{
          display: { xs: 'none', sm: 'grid' }, gridTemplateColumns: '220px 1fr', px: 2.5, py: 1,
          fontFamily: mono, fontSize: '0.7rem', color: 'text.secondary', borderBottom: '1px solid rgba(255,255,255,0.06)',
        }}
      >
        <span>requirement (coursework)</span>
        <span>in the rewrite</span>
      </Box>
      {groups.map((g) => (
        <Box key={g}>
          <Typography sx={{ px: 2.5, pt: 2, pb: 0.5, fontFamily: mono, fontSize: '0.7rem', color: 'primary.main' }}>{g.toLowerCase()}</Typography>
          {requirements.filter((r) => r.group === g).map((r) => (
            <Box
              key={r.name}
              sx={{
                display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '220px 1fr' }, gap: { xs: 0.25, sm: 2 },
                px: 2.5, py: 1.1, borderTop: '1px solid rgba(255,255,255,0.04)',
              }}
            >
              <Typography sx={{ fontWeight: 500, fontSize: '0.88rem' }}>{r.name}</Typography>
              <Typography variant="body2" color="text.secondary">{r.now}</Typography>
            </Box>
          ))}
        </Box>
      ))}
    </Paper>
  );
}

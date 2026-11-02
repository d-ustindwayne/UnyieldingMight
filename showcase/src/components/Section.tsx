import type { ReactNode } from 'react';
import { Box, Typography } from '@mui/material';
import { mono } from '../theme';

interface SectionProps {
  id: string;
  index: string;
  title: string;
  aside?: string;
  children: ReactNode;
}

export default function Section({ id, index, title, aside, children }: SectionProps) {
  return (
    <Box component="section" id={id} sx={{ scrollMarginTop: 88, mb: { xs: 7, md: 10 } }}>
      <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1.5, mb: 2.5, flexWrap: 'wrap' }}>
        <Typography sx={{ fontFamily: mono, color: 'primary.main', fontSize: '0.8rem' }}>{index}</Typography>
        <Typography variant="h2" component="h2">{title}</Typography>
        <Box sx={{ flex: 1, height: '1px', bgcolor: 'divider', minWidth: 24, alignSelf: 'center' }} />
        {aside && <Typography sx={{ fontFamily: mono, fontSize: '0.72rem', color: 'text.secondary' }}>{aside}</Typography>}
      </Box>
      {children}
    </Box>
  );
}

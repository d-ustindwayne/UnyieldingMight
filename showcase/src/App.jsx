import { useEffect, useState } from 'react';
import { Box, Button, Container, GlobalStyles, Link, Typography } from '@mui/material';
import GitHubIcon from '@mui/icons-material/GitHub';
import { REPO_SLUG, REPO_URL } from './data';
import { ember, glass, mono } from './theme';
import Intro from './components/Intro';
import Section from './components/Section';
import Architecture from './components/Architecture';
import Problems from './components/Problems';
import Metrics from './components/Metrics';
import Screens from './components/Screens';

const toc = [
  { id: 'readme', label: 'README' },
  { id: 'architecture', label: 'architecture/' },
  { id: 'problems', label: 'problems-solved/' },
  { id: 'metrics', label: 'metrics/' },
  { id: 'screens', label: 'screens/' },
];

// Highlights the section currently in view in the side index.
function useActiveSection(ids) {
  const [active, setActive] = useState(ids[0]);
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => entries.forEach((e) => e.isIntersecting && setActive(e.target.id)),
      { rootMargin: '-30% 0px -60% 0px' },
    );
    ids.forEach((id) => {
      const el = document.getElementById(id);
      if (el) observer.observe(el);
    });
    return () => observer.disconnect();
  }, [ids]);
  return active;
}

function TopBar() {
  return (
    <Box
      component="nav"
      sx={{
        ...glass, position: 'sticky', top: 12, zIndex: 10, mx: 'auto', mt: 1.5, mb: { xs: 5, md: 8 },
        borderRadius: 3, px: { xs: 1.5, sm: 2 }, py: 1, display: 'flex', alignItems: 'center', gap: 1.5,
      }}
    >
      <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'primary.main', boxShadow: `0 0 12px ${ember}` }} />
      <Typography sx={{ fontFamily: mono, fontSize: '0.8rem', flex: 1, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
        {REPO_SLUG}
      </Typography>
      <Button href={REPO_URL} target="_blank" rel="noopener" size="small" variant="outlined" startIcon={<GitHubIcon />}>
        GitHub
      </Button>
    </Box>
  );
}

function SideIndex({ active }) {
  return (
    <Box component="aside" sx={{ display: { xs: 'none', lg: 'block' }, position: 'sticky', top: 104, alignSelf: 'start' }}>
      <Typography sx={{ fontFamily: mono, fontSize: '0.72rem', color: 'text.secondary', mb: 1.5 }}>~/UnyieldingMight</Typography>
      <Box component="ul" sx={{ m: 0, p: 0, listStyle: 'none', borderLeft: '1px solid rgba(255,255,255,0.08)' }}>
        {toc.map((t) => (
          <li key={t.id}>
            <Link
              href={`#${t.id}`}
              underline="none"
              sx={{
                display: 'block', fontFamily: mono, fontSize: '0.78rem', py: 0.6, pl: 2, ml: '-1px',
                borderLeft: '1px solid', borderColor: active === t.id ? 'primary.main' : 'transparent',
                color: active === t.id ? 'text.primary' : 'text.secondary', transition: 'color .2s',
                '&:hover': { color: 'text.primary' },
              }}
            >
              {t.label}
            </Link>
          </li>
        ))}
      </Box>
    </Box>
  );
}

export default function App() {
  const active = useActiveSection(toc.map((t) => t.id));

  return (
    <>
      <GlobalStyles
        styles={{
          html: { scrollBehavior: 'smooth' },
          body: { background: '#0E0E0E', overflowX: 'hidden' },
          '@keyframes fadeIn': { from: { opacity: 0, transform: 'translateY(4px)' }, to: { opacity: 1, transform: 'none' } },
          '@media (prefers-reduced-motion: reduce)': { html: { scrollBehavior: 'auto' }, '*': { animation: 'none !important' } },
        }}
      />
      {/* Ambient glow behind the glass panels. */}
      <Box
        aria-hidden
        sx={{
          position: 'fixed', inset: 0, zIndex: -1, pointerEvents: 'none',
          background: `
            radial-gradient(600px 400px at 85% 10%, rgba(247,151,29,0.16), transparent 70%),
            radial-gradient(500px 500px at 5% 70%, rgba(143,211,168,0.07), transparent 70%),
            linear-gradient(rgba(255,255,255,0.025) 1px, transparent 1px) 0 0 / 100% 32px`,
        }}
      />

      <Container maxWidth="lg" sx={{ px: { xs: 2, sm: 3 } }}>
        <TopBar />
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', lg: '170px 1fr' }, gap: { lg: 6 } }}>
          <SideIndex active={active} />
          <Box component="main" sx={{ minWidth: 0 }}>
            <Intro />
            <Section id="architecture" index="01" title="Architecture" aside="click a module">
              <Architecture />
            </Section>
            <Section id="problems" index="02" title="Problems solved" aside="real excerpts">
              <Problems />
            </Section>
            <Section id="metrics" index="03" title="Metrics">
              <Metrics />
            </Section>
            <Section id="screens" index="04" title="Screens" aside="scroll →">
              <Screens />
            </Section>
          </Box>
        </Box>

        <Box
          component="footer"
          sx={{ borderTop: '1px solid', borderColor: 'divider', py: 4, mb: 2, display: 'flex', flexWrap: 'wrap', gap: 2, justifyContent: 'space-between' }}
        >
          <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 560 }}>
            Academic portfolio project, not a commercial product. Payments use Stripe test mode. Began as a group project with{' '}
            <Link href="https://github.com/SaplingBeing" target="_blank" rel="noopener">SaplingBeing</Link> and{' '}
            <Link href="https://github.com/darawesome" target="_blank" rel="noopener">darawesome</Link>; the Kotlin rewrite is by{' '}
            <Link href="https://github.com/d-ustindwayne" target="_blank" rel="noopener">d-ustindwayne</Link>.
          </Typography>
          <Link href={REPO_URL} target="_blank" rel="noopener" sx={{ fontFamily: mono, fontSize: '0.8rem' }}>
            source →
          </Link>
        </Box>
      </Container>
    </>
  );
}

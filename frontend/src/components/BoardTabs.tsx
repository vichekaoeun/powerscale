import { NavLink } from "react-router-dom";

interface BoardTabsProps {
  voteTo: string;
  rankingsTo: string;
}

const tabClassName = ({ isActive }: { isActive: boolean }) =>
  isActive ? "board-tab active" : "board-tab";

export function BoardTabs({ voteTo, rankingsTo }: BoardTabsProps) {
  return (
    <nav className="board-tabs" aria-label="Board sections">
      <NavLink className={tabClassName} end to={voteTo}>
        Vote
      </NavLink>
      <NavLink className={tabClassName} to={rankingsTo}>
        Rankings
      </NavLink>
    </nav>
  );
}

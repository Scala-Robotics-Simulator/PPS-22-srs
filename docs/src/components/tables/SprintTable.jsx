export function SprintTable({ data }) {
  // Determine the maximum number of days from the data
  const maxDays = data.length > 0 ? Math.max(...data.map(task => task.days.length)) : 0;
  const dayTotals = Array(maxDays).fill(0);
  
  const effettivoTot = data.reduce((acc, task) => {
    task.days.forEach((d, i) => (dayTotals[i] += d));
    return acc + task.effettivo;
  }, 0);
  const stimaTot = data.reduce((acc, task) => {
    return acc + task.stima;
  }, 0);
  // TODO: calculate days and effective time programmatically
  return (
    <table>
      <thead>
        <tr>
          <th>Backlog Item</th>
          <th>Id</th>
          <th>Task</th>
          <th>Volontario</th>
          <th>Stima (h)</th>
          <th>Effettivo (h)</th>
          {Array.from({ length: maxDays }, (_, i) => (
            <th key={i}>D{i + 1}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {data.map(({ backlogItem, id, task, volontario, stima, effettivo, days }, i) => (
          <tr key={i}>
            <td>{backlogItem}</td>
            <td>{id}</td>
            <td>{task}</td>
            <td>{volontario}</td>
            <td>{stima}</td>
            <td>{effettivo}</td>
            {days.map((d, i) => (
              <td key={i}>{d}</td>
            ))}
          </tr>
        ))}
        <tr>
          <td colSpan={2}></td>
          <td><strong>TOT</strong></td>
          <td></td>
          <td><strong>{stimaTot}</strong></td>
          <td><strong>{effettivoTot}</strong></td>
          {dayTotals.map((t, i) => (
            <td key={i}><strong>{t}</strong></td>
          ))}
        </tr>
      </tbody>
    </table>
  );
}